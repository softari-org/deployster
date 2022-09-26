package opstopus.deploptopus.github.deploy

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.curl.Curl
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import opstopus.deploptopus.InternalServerError
import opstopus.deploptopus.github.GitHubAuth
import opstopus.deploptopus.github.JWT
import opstopus.deploptopus.github.events.DeploymentPayload
import opstopus.deploptopus.github.events.GitHubAccessTokenPayload
import opstopus.deploptopus.github.events.InstallationPayload
import opstopus.deploptopus.github.events.SerializableURL
import opstopus.deploptopus.serializers.jsonFormatter

internal object DeploymentStateSerializer : KSerializer<DeploymentState> {
    // TODO this class is very similar to EventTypeSerializer. Should be factored out if possible
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "DeploymentState",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: DeploymentState) {
        encoder.encodeString(value.state)
    }

    override fun deserialize(decoder: Decoder): DeploymentState {
        return DeploymentState[decoder.decodeString()]
    }
}

@Serializable(with = DeploymentStateSerializer::class)
enum class DeploymentState(val state: String) {
    ERROR("error"),
    FAILURE("failure"),
    INACTIVE("inactive"),
    IN_PROGRESS("in_progress"),
    QUEUED("queued"),
    PENDING("pending"),
    SUCCESS("success");

    companion object {
        operator fun get(state: String) = DeploymentState.values().find { it.state == state }
            ?: throw IllegalArgumentException("Unknown deployment state $state")
    }
}

@Serializable
data class DeploymentStatusCreateRequestPayload(
    val state: DeploymentState,
    @SerialName("target_url") val targetURL: SerializableURL? = null,
    @SerialName("log_url") val logURL: SerializableURL? = null,
    val description: String? = null,
    val environment: String? = null,
    @SerialName("environment_url") val environmentURL: SerializableURL? = null,
    @SerialName("auto_inactive") val autoInactive: Boolean? = null
)

/**
 * Manages deployment statuses for an app installation
 */
class DeploymentStatus(private val installation: InstallationPayload) {
    private val accessToken = this.refreshAccessToken(this.installation)
        ?: throw NullPointerException("Could not get initial GitHub access token.")
        get() {
            if (field.isExpired()) {
                DeploymentStatus.logger.debug("Refreshing GitHub access token.")
                return this.refreshAccessToken(this.installation) ?: field
            }
            return field
        }

    constructor(installationID: UInt) : this(
        runBlocking {
            DeploymentStatus.client.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.github.com"
                    path("app", "installations", installationID.toString())
                }
                bearerAuth(DeploymentStatus.installationToken.jws)
            }.let {
                if (!it.status.isSuccess()) {
                    throw InternalServerError("Could not fetch installtion from GitHub API.")
                }
                return@runBlocking it.body()
            }
        }
    )

    /**
     * Update the status of an existing deployment
     */
    suspend fun update(deployment: DeploymentPayload, state: DeploymentState) {
        val response = DeploymentStatus.client.post(deployment.statusesURL) {
            bearerAuth(this@DeploymentStatus.accessToken.token)
            setBody(
                DeploymentStatusCreateRequestPayload(
                    state,
                    description = "Deployed by deploptopus",
                    environment = deployment.environment,
                    autoInactive = true
                )
            )
        }

        if (response.status.isSuccess()) {
            DeploymentStatus.logger.info(
                "Succesfully updated status of ${deployment.id} to ${state.state}."
            )
        } else {
            DeploymentStatus.logger.error(
                "Failed to update status of ${deployment.id} to ${state.state}"
            )
        }
    }

    /**
     * Refresh the GitHub access token for a specific app installation
     */
    private fun refreshAccessToken(installation: InstallationPayload): GitHubAccessTokenPayload? {
        return runBlocking {
            val response = DeploymentStatus.client.post(installation.accessTokensURL) {
                bearerAuth(DeploymentStatus.installationToken.jws)
            }
            if (response.status.isSuccess()) {
                DeploymentStatus.logger.info(
                    "Successfully refreshed access token for installation ${installation.id}"
                )
                return@runBlocking response.body()
            } else {
                DeploymentStatus.logger.info(
                    "Failed to refresh access token for installation ${installation.id}"
                )
                return@runBlocking null
            }
        }
    }

    companion object {
        private val client = HttpClient(Curl) {
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType("application", "vnd.github+json"))
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                exponentialDelay()
            }
            install(Logging)
            install(ContentNegotiation) {
                json(jsonFormatter)
            }
        }
        private val installationToken: JWT = JWT(GitHubAuth.GITHUB_APP_PRIVATE_KEY)
            get() {
                if (field.isExpired()) {
                    this.logger.debug("Refreshing installation token.")
                    return JWT(GitHubAuth.GITHUB_APP_PRIVATE_KEY)
                }
                return field
            }
        private val logger = KtorSimpleLogger("DeploymentStatus")
    }
}
