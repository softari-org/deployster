package opstopus.deploptopus.github

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.addressOf
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
import openssl.EVP_sha256
import openssl.HMAC
import openssl.OPENSSL_buf2hexstr
import openssl.SHA256_DIGEST_LENGTH
import openssl.opensslFree
import opstopus.deploptopus.Forbidden
import opstopus.deploptopus.InternalServerError

object Crypto {
    // The maximum length for an error created by OpenSSL
    private const val OPENSSL_ERROR_MESSAGE_LENGTH: Int = 256

    // Logger
    private val log = KtorSimpleLogger("Crypto")

    /**
     * Fetch the most recent error message from OpenSSL
     */
    private fun getErrorMessage(): String {
        ByteArray(Crypto.OPENSSL_ERROR_MESSAGE_LENGTH).usePinned {
            // Use OpenSSL's error stack to fetch the error message
            ERR_error_string_n(
                ERR_get_error(),
                it.addressOf(0),
                Crypto.OPENSSL_ERROR_MESSAGE_LENGTH.convert()
            )
            return it.get().toKString()
        }
    }

    /**
     * Compute the cryptographic HMAC SHA256 signature of an arbitrary string
     */
    fun computeSignature(body: String, secret: String): String {
        memScoped {
            // Buffer to store the length of the output signature
            val computedLength = this.alloc<UIntVar>()
            val signatureBuf = this.allocArray<UByteVar>(SHA256_DIGEST_LENGTH)

            // Compute the HMAC SHA256 signature of the request body
            val hmac = run {
                secret.usePinned {
                    body.usePinned {
                        return@run HMAC(
                            EVP_sha256(),
                            secret.cstr,
                            secret.length,
                            body.cstr.ptr.reinterpret(),
                            body.length.convert(),
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

            var hexSignatureRaw: CPointer<ByteVar>? = null
            val hexSignature: String
            try {
                // Encode the signature as hexadecimal
                hexSignatureRaw = OPENSSL_buf2hexstr(
                    signatureBuf.getPointer(this),
                    computedLength.value.toLong()
                )
                hexSignature = hexSignatureRaw?.toKString()
                    ?: throw InternalServerError("Could not hex encode signature")
            } finally {
                // Free the memory allocated by OpenSSL
                hexSignatureRaw?.let {
                    opensslFree(it.getPointer(this))
                }
            }

            return "sha256=${hexSignature.lowercase().filterNot { it == ':' }}"
        }
    }

    /**
     * Verify the signature of an incoming request.
     *
     * @throws Forbidden if the signature is not correct
     */
    fun verifySignature(body: String, secret: String, signature: String) {
        // Buffer to store the content of the output signature
        val computedSignature = this.computeSignature(body, secret)

        // If the computed signature does not match the provided signature,
        // then the caller used the wrong secret
        if (signature != computedSignature) {
            this.log.debug(
                "Computed signature $computedSignature does not match provided signature $signature"
            )
            throw Forbidden("Request failed signature check")
        }
    }
}
