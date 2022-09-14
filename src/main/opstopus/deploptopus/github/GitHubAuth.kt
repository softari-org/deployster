package opstopus.deploptopus.github

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import openssl.EVP_PKEY
import openssl.EVP_PKEY_free
import openssl.PEM_read_PrivateKey
import opstopus.deploptopus.GlobalLifecycle
import opstopus.deploptopus.MissingSecretException
import opstopus.deploptopus.system.FileIO
import platform.posix.getenv
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Manages authentication functions
 */
object GitHubAuth {
    val GITHUB_APP_PRIVATE_KEY = this.resolvePrivateKey()

    private fun resolvePrivateKey(): PrivateKeyRSA {
        val path = getenv("GITHUB_APP_PRIVATE_PEM_KEYFILE")?.toKString()
            ?: throw MissingSecretException("Path to private pem keyfile not provided")

        return PrivateKeyRSA(path)
    }
}

/**
 * Lifecycle manager for OpenSSL Private Key structs
 */
class PrivateKeyRSA(val ptr: CPointer<EVP_PKEY>) {
    init {
        GlobalLifecycle.manage { EVP_PKEY_free(this.ptr) }
    }

    constructor(path: String) : this(PrivateKeyRSA.readFromFile(path))

    companion object {
        private fun readFromFile(path: String): CPointer<EVP_PKEY> = memScoped {
            val pkeyIO = FileIO(path)

            return PEM_read_PrivateKey(pkeyIO.file.reinterpret(), null, null, null)
                ?: throw MissingSecretException(Crypto.getErrorMessage())
        }
    }
}

/**
 * Represents the minimum required implementation for a JavaScript Web Token
 */
class JWT(private val privateKeyRSA: PrivateKeyRSA) {
    private val iat: Instant by lazy { Clock.System.now() - 60.seconds }
    private val exp: Instant by lazy { this.iat + 10.minutes }

    val jws: String by lazy {
        val payload = Crypto.encodeBase64URL(
            buildString {
                append("{\"iat\":${this@JWT.iat.epochSeconds},")
                append("\"exp\":${this@JWT.exp.epochSeconds},")
                append("\"iss\":\"${JWT.githubAppID}\"}")
            }
        )
        val signature = Crypto.signRS256("${JWT.header}.$payload", this.privateKeyRSA)
        return@lazy "${JWT.header}.$payload.$signature"
    }

    fun isExpired(): Boolean = this.exp >= Clock.System.now() - 60.seconds

    companion object {
        private val header = Crypto.encodeBase64URL("{\"alg\":\"RS256\",\"typ\":\"JWT\"}")
        private val githubAppID = getenv("GITHUB_APP_ID")?.toKString()
            ?: throw IllegalArgumentException("Missing GitHub app ID")
    }
}
