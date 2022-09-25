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
data class LicensePayload(
    val key: String,
    val name: String,
    @SerialName("spdx_id") val spdxID: String,
    val url: SerializableURL,
    @SerialName("node_id") val nodeID: String
)

@Serializable
data class RepositoryPayload(
    val id: UInt,
    @SerialName("node_id") val nodeID: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("private") val isPrivate: Boolean,
    val owner: SenderPayload,
    val organization: OrganizationPayload?,
    @SerialName("html_url") val htmlURL: SerializableURL,
    val description: String?,
    val fork: Boolean,
    val url: SerializableURL,
    @SerialName("forks_url") val forksURL: SerializableURL,
    @SerialName("keys_url") val keysURL: SerializableURL,
    @SerialName("collaborators_url") val collaboratorsURL: SerializableURL,
    @SerialName("teams_url") val teamsURL: SerializableURL,
    @SerialName("hooks_url") val hooksURL: SerializableURL,
    @SerialName("issue_events_url") val issueEventsURL: SerializableURL,
    @SerialName("events_url") val eventsURL: SerializableURL,
    @SerialName("assignees_url") val assigneesURL: SerializableURL,
    @SerialName("branches_url") val branchesURL: SerializableURL,
    @SerialName("tags_url") val tagsURL: SerializableURL,
    @SerialName("blobs_url") val blobsURL: SerializableURL,
    @SerialName("git_tags_url") val gitTagsURL: SerializableURL,
    @SerialName("git_refs_url") val gitRefsURL: SerializableURL,
    @SerialName("trees_url") val treesURL: SerializableURL,
    @SerialName("statuses_url") val statusesURL: SerializableURL,
    @SerialName("languages_url") val languagesURL: SerializableURL,
    @SerialName("stargazers_url") val stargazersURL: SerializableURL,
    @SerialName("contributors_url") val contributorsURL: SerializableURL,
    @SerialName("subscribers_url") val subscribersURL: SerializableURL,
    @SerialName("subscription_url") val subscriptionURL: SerializableURL,
    @SerialName("commits_url") val commitsURL: SerializableURL,
    @SerialName("git_commits_url") val gitCommitsURL: SerializableURL,
    @SerialName("comments_url") val commentsURL: SerializableURL,
    @SerialName("issue_comment_url") val issueCommentsURL: SerializableURL,
    @SerialName("contents_url") val contentsURL: SerializableURL,
    @SerialName("compare_url") val compareURL: SerializableURL,
    @SerialName("merges_url") val mergesURL: SerializableURL,
    @SerialName("archive_url") val archiveURL: SerializableURL,
    @SerialName("downloads_url") val downloadsURL: SerializableURL,
    @SerialName("issues_url") val issuesURL: SerializableURL,
    @SerialName("pulls_url") val pullsURL: SerializableURL,
    @SerialName("milestones_url") val milestonesURL: SerializableURL,
    @SerialName("notifications_url") val notificationsURL: SerializableURL,
    @SerialName("labels_url") val labelsURL: SerializableURL,
    @SerialName("releases_url") val releasesURL: SerializableURL,
    @SerialName("deployments_url") val deploymentsURL: SerializableURL,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("pushed_at") val pushedAt: Instant,
    @SerialName("git_url") val gitURL: SerializableURL,
    @SerialName("ssh_url") val sshURL: String,
    @SerialName("clone_url") val cloneURL: SerializableURL,
    @SerialName("svn_url") val svnURL: SerializableURL,
    val homepage: SerializableURL?,
    val size: UInt,
    @SerialName("stargazers_count") val stargazersCount: UInt,
    @SerialName("watchers_count") val watchersCount: UInt,
    val language: String,
    @SerialName("has_issues") val hasIssues: Boolean,
    @SerialName("has_projects") val hasProjects: Boolean,
    @SerialName("has_downloads") val hasDownloads: Boolean,
    @SerialName("has_wiki") val hasWiki: Boolean,
    @SerialName("has_pages") val hasPages: Boolean,
    @SerialName("forks_count") val forksCount: UInt,
    @SerialName("mirror_url") val mirrorURL: SerializableURL?,
    val archived: Boolean,
    val disabled: Boolean,
    @SerialName("open_issues_count") val openIssuesCount: UInt,
    val license: LicensePayload?,
    @SerialName("allow_forking") val allowForking: Boolean,
    @SerialName("is_template") val isTemplate: Boolean,
    val topics: List<String>,
    val visibility: String,
    @SerialName("web_commit_signoff_required") val webCommitSignoffRequired: Boolean,
    val forks: UInt,
    @SerialName("open_issues") val openIssues: UInt,
    val watchers: UInt,
    @SerialName("default_branch") val defaultBranch: String,
    @SerialName("temp_clone_token") val tempCloneToken: String?
)

@Serializable
data class PartialRepositoryPayload(
    val id: UInt,
    @SerialName("node_id") val nodeID: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("private") val isPrivate: Boolean
)

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
data class InstallationPayload(
    val id: UInt,
    val account: SenderPayload,
    @SerialName("repository_selection") val repositorySelection: String,
    @SerialName("access_tokens_url") val accessTokensURL: SerializableURL,
    @SerialName("repositories_url") val repositoriesURL: SerializableURL,
    @SerialName("html_url") val htmlURL: SerializableURL,
    @SerialName("app_id") val appID: UInt,
    @SerialName("target_id") val targetID: UInt,
    @SerialName("target_type") val targetType: String,
    val events: List<String>,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("single_file_name") val singleFileName: String?,
    @SerialName("app_slug") val appSlug: String,
    @SerialName("suspended_by") val suspendedBy: SenderPayload?,
    @SerialName("suspended_at") val suspendedAt: Instant?,
    @SerialName("contact_email") val contactEmail: String? = null,
    val repositories: List<PartialRepositoryPayload>? = null,
    val sender: SenderPayload? = null,
    @SerialName("has_multiple_single_files") val hasMultipleFiles: Boolean? = null,
    @SerialName("single_file_paths") val singleFilePaths: List<String>? = null
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
    abstract val installation: InstallationPayload?
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
    override val installation: InstallationPayload? = null
) : EventPayload()

/**
 * Represents the body of a response for a GitHub access token
 */
@Serializable
data class GitHubAccessTokenPayload(
    val token: String,
    @SerialName("expires_at") val expiresAt: Instant,
    @SerialName("repository_selection") val repositorySelection: String,
    val repositories: List<RepositoryPayload>? = null
) {
    fun isExpired(): Boolean = this.expiresAt <= Clock.System.now() - 60.seconds
}
