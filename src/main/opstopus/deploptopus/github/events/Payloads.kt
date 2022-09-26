package opstopus.deploptopus.github.events

import io.ktor.http.Url
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import opstopus.deploptopus.serializers.URLSerializer
import kotlin.time.Duration.Companion.seconds

typealias SerializableURL = (
    @Serializable(with = URLSerializer::class)
    Url
)

@Serializable
data class SenderPayload(
    val login: String,
    val id: UInt,
    @SerialName("node_id") val nodeID: String,
    @SerialName("avatar_url") val avatarURL: SerializableURL,
    @SerialName("gravatar_id") val gravatarID: String,
    val url: SerializableURL,
    @SerialName("html_url") val htmlURL: SerializableURL,
    @SerialName("followers_url") val followersURL: SerializableURL,
    @SerialName("following_url") val followingURL: SerializableURL,
    @SerialName("gists_url") val gistsURL: SerializableURL,
    @SerialName("starred_url") val starredURL: SerializableURL,
    @SerialName("subscriptions_url") val subscriptionsURL: SerializableURL,
    @SerialName("organizations_url") val organizationsURL: SerializableURL,
    @SerialName("repos_url") val reposURL: SerializableURL,
    @SerialName("events_url") val eventsURL: SerializableURL,
    @SerialName("received_events_url") val receivedEventsURL: SerializableURL,
    val type: String,
    @SerialName("site_admin") val siteAdmin: Boolean
)

@Serializable
data class RepositoryPayload(val name: String, @SerialName("full_name") val fullName: String)

@Serializable
data class OrganizationPayload(
    val login: String,
    val id: UInt,
    @SerialName("node_id") val nodeID: String,
    @SerialName("gravatar_id") val gravatarID: String,
    val url: SerializableURL,
    @SerialName("html_url") val htmlURL: SerializableURL,
    @SerialName("repos_url") val reposURL: SerializableURL,
    @SerialName("gists_url") val gistsURL: SerializableURL,
    @SerialName("events_url") val eventsURL: SerializableURL,
    @SerialName("hooks_url") val hooksURL: SerializableURL?,
    @SerialName("issues_url") val issuesURL: SerializableURL?,
    @SerialName("members_url") val membersURL: SerializableURL?,
    @SerialName("public_members_url") val publicMembersURL: SerializableURL?,
    @SerialName("followers_url") val followersURL: SerializableURL,
    @SerialName("following_url") val followingURL: SerializableURL,
    @SerialName("avatar_url") val avatarURL: SerializableURL,
    @SerialName("gravatar_url") val gravatarURL: SerializableURL?,
    val description: String?
)

@Serializable
data class PartialInstallationPayload(val id: UInt)

@Serializable
data class InstallationPayload(
    val id: UInt,
    @SerialName("access_tokens_url") val accessTokensURL: SerializableURL
)

/**
 * Represents the common properties shared by every event payload. All event payload
 * data classes must implement this class.
 */
@Serializable
sealed class EventPayload {
    abstract val action: String?
    abstract val sender: SenderPayload
    abstract val repository: RepositoryPayload?
    abstract val organization: OrganizationPayload?
    abstract val installation: PartialInstallationPayload?
}

@Serializable
data class DeploymentPayload(
    val url: SerializableURL,
    val id: UInt,
    @SerialName("node_id") val nodeID: String,
    val sha: String,
    val ref: String,
    val task: String,
    val payload: JsonElement,
    val environment: String,
    val description: String?,
    val creator: SenderPayload?,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("statuses_url") val statusesURL: SerializableURL,
    @SerialName("repository_url") val repositoryURL: SerializableURL
)

/**
 * Represents the body of a deployment event payload
 */
@Serializable
data class DeploymentEventPayload(
    override val action: String,
    val deployment: DeploymentPayload,
    override val repository: RepositoryPayload,
    override val sender: SenderPayload,
    override val organization: OrganizationPayload? = null,
    override val installation: PartialInstallationPayload? = null
) : EventPayload()

/**
 * Represents the body of a response for a GitHub access token
 */
@Serializable
data class GitHubAccessTokenPayload(
    val token: String,
    @SerialName("expires_at") val expiresAt: Instant
) {
    fun isExpired(): Boolean = this.expiresAt >= Clock.System.now() - 60.seconds
}
