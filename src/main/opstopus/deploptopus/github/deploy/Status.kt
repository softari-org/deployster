package opstopus.deploptopus.github.deploy

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.curl.Curl
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.append
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import opstopus.deploptopus.github.GitHubAuth
import opstopus.deploptopus.github.JWT
import opstopus.deploptopus.github.events.DeploymentPayload
import opstopus.deploptopus.github.events.GitHubAccessTokenPayload
import opstopus.deploptopus.github.events.GitHubAccessTokenRequestPayload
import opstopus.deploptopus.github.events.InstallationPayload
import opstopus.deploptopus.github.events.PermissionsPayload
import opstopus.deploptopus.github.events.SenderPayload
import opstopus.deploptopus.github.events.SerializableURL

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
    ERROR("error"), FAILURE("failure"), INACTIVE("inactive"), IN_PROGRESS("in_progress"), QUEUED("queued"), PENDING(
        "pending"
    ),
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

@Serializable
data class GitHubAppPayload(
    val id: UInt,
    val slug: String,
    @SerialName("node_id") val nodeID: String,
    val owner: SenderPayload,
    val name: String,
    val description: String?,
    @SerialName("external_url") val externalURL: SerializableURL,
    @SerialName("html_url") val htmlURL: SerializableURL,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    val permissions: PermissionsPayload
)

/**
 * Manages deployment statuses for an app installation
 */
class DeploymentStatus(private val installation: InstallationPayload) {

    private val accessToken: GitHubAccessTokenPayload = this.refreshAccessToken(this.installation)
        ?: throw NullPointerException("Could not get initial GitHub access token.")
        get() {
            if (field.isExpired()) {
                return this.refreshAccessToken(this.installation) ?: field
            }
            return field
        }

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
                bearerAuth(DeploymentStatus.authzToken.jws)
                setBody(
                    GitHubAccessTokenRequestPayload(
                        permissions = installation.permissions,
                        repositories = installation.repositories?.map { it.name }
                    )
                )
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
                headers {
                    append(HttpHeaders.ContentType, ContentType.Application.Json)
                    append(HttpHeaders.Accept, ContentType("application", "vnd.github+json"))
                }
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                exponentialDelay()
            }
            install(Logging)
            install(ContentNegotiation) {
                json()
            }
        }
        private val authzToken: JWT = JWT(GitHubAuth.GITHUB_APP_PRIVATE_KEY)
            get() {
                if (field.isExpired()) {
                    return JWT(GitHubAuth.GITHUB_APP_PRIVATE_KEY)
                }
                return field
            }
        private val logger = KtorSimpleLogger("DeploymentStatus")
    }
}
