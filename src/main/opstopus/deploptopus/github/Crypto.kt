package opstopus.deploptopus.github

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import openssl.ERR_error_string_n
import openssl.ERR_get_error
import openssl.EVP_DigestSignFinal
import openssl.EVP_DigestSignInit
import openssl.EVP_DigestUpdate
import openssl.EVP_EncodeBlock
import openssl.EVP_MD_CTX_free
import openssl.EVP_MD_CTX_new
import openssl.EVP_sha256
import openssl.HMAC
import openssl.OPENSSL_buf2hexstr
import openssl.SHA256_DIGEST_LENGTH
import openssl.opensslFree
import opstopus.deploptopus.InternalServerError
import opstopus.deploptopus.MemoryAllocationException
import platform.posix.size_tVar

object Crypto {
    // The maximum length for an error created by OpenSSL
    private const val OPENSSL_ERROR_MESSAGE_LENGTH = 256UL

    // Logger
    private val log = KtorSimpleLogger("Crypto")

    /**
     * Fetch the most recent error message from OpenSSL
     */
    fun getErrorMessage(): String = memScoped {
        val errorBuf = this.allocArray<ByteVar>(Crypto.OPENSSL_ERROR_MESSAGE_LENGTH.convert())
        // Use OpenSSL's error stack to fetch the error message
        ERR_error_string_n(
            ERR_get_error(),
            errorBuf,
            Crypto.OPENSSL_ERROR_MESSAGE_LENGTH
        )
        return errorBuf.toKString()
    }

    /**
     * Compute the cryptographic HMAC SHA256 signature of an arbitrary string
     */
    fun signHMACSHA256(message: String, secret: String): String = memScoped {
        // Buffer to store the length of the output signature
        val computedLength = this.alloc<UIntVar>()
        val signatureBuf = this.allocArray<UByteVar>(SHA256_DIGEST_LENGTH)

        // Compute the HMAC SHA256 signature of the request body
        val hmac = run {
            secret.usePinned {
                message.usePinned {
                    return@run HMAC(
                        EVP_sha256(),
                        secret.cstr,
                        secret.length,
                        message.cstr.ptr.reinterpret(),
                        message.length.convert(),
                        signatureBuf.getPointer(this),
                        computedLength.ptr
                    )
                }
            }
        }

        // Check if there were any errors computing the HMAC
        if (hmac == null) {
            // Log the error, and respond to the caller
            Crypto.log.error(Crypto.getErrorMessage())
            throw InternalServerError(
                "Failed to compute HMAC SHA256 signature for request"
            )
        }

        // Encode the signature as hexadecimal
        val hexSignature =
            OPENSSL_buf2hexstr(signatureBuf.getPointer(this), computedLength.value.toLong())
                ?: throw InternalServerError("Could not hex encode signature")

        this.defer { opensslFree(hexSignature) }

        return "sha256=${hexSignature.toKString().lowercase().filterNot { it == ':' }}"
    }

    /**
     * Produce the RS256 signature for a message
     */
    fun signRS256(message: String, privateKey: PrivateKeyRSA): String = memScoped {
        // Declare the envelope message digest context
        val evpMDCTX = EVP_MD_CTX_new() ?: throw MemoryAllocationException()
        this.defer { EVP_MD_CTX_free(evpMDCTX) }

        // Tell the context that we are making a SHA256 digest
        if (EVP_DigestSignInit(evpMDCTX, null, EVP_sha256(), null, privateKey.ptr) == 0) {
            throw InternalServerError(Crypto.getErrorMessage())
        }

        message.usePinned {
            // Hash the message
            if (EVP_DigestUpdate(evpMDCTX, message.cstr.ptr, message.length.convert()) == 0) {
                throw InternalServerError(Crypto.getErrorMessage())
            }
        }

        // Get length of signature
        val siglen = this.alloc<size_tVar>()
        if (EVP_DigestSignFinal(evpMDCTX, null, siglen.ptr) == 0) {
            throw InternalServerError(Crypto.getErrorMessage())
        }

        // Finalize the digest
        val sig = this.allocArray<ByteVar>(siglen.value.convert())
        if (EVP_DigestSignFinal(evpMDCTX, sig.reinterpret(), siglen.ptr) == 0) {
            throw InternalServerError(Crypto.getErrorMessage())
        }

        return Crypto.encodeBase64URL(sig, siglen.value.toInt())
    }

    /**
     * Verify the signature of an incoming request.
     */
    fun signatureIsValid(body: String, secret: String, signature: String?): Boolean {
        // Buffer to store the content of the output signature
        val computedSignature = this.signHMACSHA256(body, secret)

        // If the computed signature does not match the provided signature,
        // then the caller used the wrong secret
        if (signature != computedSignature) {
            this.log.debug(
                "Computed signature $computedSignature does not match provided signature $signature"
            )
            return false
        }
        return true
    }

    /**
     * Encodes a message in Base64
     */
    fun encodeBase64URL(message: String): String = memScoped {
        message.usePinned {
            return@memScoped Crypto.encodeBase64URL(message.cstr.ptr, message.length)
        }
    }

    private fun encodeBase64URL(message: CArrayPointer<ByteVar>, length: Int): String = memScoped {
        val targetLength = Crypto.getBase64Length(length)
        val b64buf = this.allocArray<ByteVar>(targetLength + 1)
        EVP_EncodeBlock(
            b64buf.reinterpret(),
            message.reinterpret(),
            length
        )

        return b64buf.toKString().replace("+", "-").replace("/", "_").trimEnd { it == '=' }
    }

    private fun getBase64Length(message: String) = this.getBase64Length(message.length)
    private fun getBase64Length(length: Int) = 4 * ((length + 1) / 3)
}
