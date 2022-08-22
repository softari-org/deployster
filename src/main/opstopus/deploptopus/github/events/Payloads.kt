package opstopus.deploptopus.github.events

import io.ktor.http.ContentType
import io.ktor.http.Url
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import opstopus.deploptopus.ContentTypeSerializer
import opstopus.deploptopus.URLSerializer

typealias SerializableURL = (
    @Serializable(with = URLSerializer::class)
    Url
)

typealias SerializableContentType = (
    @Serializable(with = ContentTypeSerializer::class)
    ContentType
)

@Serializable
data class SenderPayload(
    val login: String,
    val id: Int,
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
data class RepositoryPayload(
    val id: Int,
    @SerialName("node_id") val nodeID: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("private") val isPrivate: Boolean,
    val owner: SenderPayload,
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
    val size: Int,
    @SerialName("stargazers_count") val stargazersCount: Int,
    @SerialName("watchers_count") val watchersCount: Int,
    val language: String,
    @SerialName("has_issues") val hasIssues: Boolean,
    @SerialName("has_projects") val hasProjects: Boolean,
    @SerialName("has_downloads") val hasDownloads: Boolean,
    @SerialName("has_wiki") val hasWiki: Boolean,
    @SerialName("has_pages") val hasPages: Boolean,
    @SerialName("forks_count") val forksCount: Int,
    @SerialName("mirror_url") val mirrorURL: SerializableURL?,
    val archived: Boolean,
    val disabled: Boolean,
    @SerialName("open_issues_count") val openIssuesCount: Int,
    val license: String?,
    val forks: Int,
    @SerialName("open_issues") val openIssues: Int,
    val watchers: Int,
    @SerialName("default_branch") val defaultBranch: String
)

@Serializable
data class PartialRepositoryPayload(
    val id: Int,
    @SerialName("node_id") val nodeID: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("private") val isPrivate: Boolean
)

@Serializable
data class OrganizationPayload(
    val login: String,
    val id: Int,
    @SerialName("node_id") val nodeID: String,
    val url: SerializableURL,
    @SerialName("repos_url") val reposURL: SerializableURL,
    @SerialName("events_url") val eventsURL: SerializableURL,
    @SerialName("hooks_url") val hooksURL: SerializableURL,
    @SerialName("issues_url") val issuesURL: SerializableURL,
    @SerialName("members_url") val membersURL: SerializableURL,
    @SerialName("public_members_url") val publicMembersURL: SerializableURL,
    @SerialName("avatar_url") val avatarURL: SerializableURL,
    val description: String?
)

@Serializable
data class PermissionsPayload(
    val metadata: String,
    val contents: String,
    val issues: String
)

@Serializable
data class InstallationPayload(
    val id: Int,
    val account: SenderPayload,
    @SerialName("repository_selection") val repositorySelection: String,
    @SerialName("access_tokens_url") val accessTokensURL: SerializableURL,
    @SerialName("repositories_url") val repositoriesURL: SerializableURL,
    @SerialName("html_url") val htmlURL: SerializableURL,
    @SerialName("app_id") val appID: Int,
    @SerialName("target_id") val targetID: Int,
    @SerialName("target_type") val targetType: String,
    val permissions: PermissionsPayload,
    val events: List<String>,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("single_file_name") val singleFileName: String,
    val repositories: List<PartialRepositoryPayload>,
    val sender: SenderPayload
)

/**
 * Represents the common properties shared by every event payload. All event payload
 * data classes must implement this class.
 */
@Serializable
sealed class EventPayload {
    abstract val action: String
    abstract val sender: SenderPayload
    abstract val repository: RepositoryPayload?
    abstract val organization: OrganizationPayload?
    abstract val installation: InstallationPayload?
}

@Serializable
data class FilePayload(
    @SerialName("download_url") val downloadURL: SerializableURL,
    val id: Int,
    val name: String,
    val sha256: String,
    val sha1: String,
    val md5: String,
    @SerialName("content_type") val contentType: SerializableContentType,
    val state: String,
    val size: Int,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant
)

@Serializable
data class ReleasePayload(
    val url: SerializableURL,
    @SerialName("html_url") val htmlUrl: SerializableURL,
    val id: Int,
    @SerialName("tag_name") val tagName: String,
    @SerialName("target_commitish") val targetCommitish: String,
    val name: String,
    val draft: Boolean,
    val author: SenderPayload,
    val prerelease: Boolean,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("published_at") val publishedAt: Instant
)

@Serializable
data class PackageVersionPayload(
    val id: Int,
    val version: String,
    val summary: String,
    val name: String,
    val description: String?,
    val body: String,
    @SerialName("body_html") val bodyHTML: String,
    val release: ReleasePayload,
    val manifest: String,
    @SerialName("html_url") val htmlURL: SerializableURL,
    @SerialName("tag_name") val tagName: String,
    @SerialName("target_commitish") val targetCommitish: String,
    @SerialName("target_oid") val targetOID: String,
    val draft: Boolean,
    val prerelease: Boolean,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    val metadata: List<String>,
    @SerialName("docker_metadata") val dockerMetadata: List<String>,
    @SerialName("package_files") val packageFiles: List<FilePayload>,
    val author: SenderPayload,
    @SerialName("source_url") val sourceURL: SerializableURL,
    @SerialName("installation_command") val installationCommand: String
)

@Serializable
data class PackageRegistryPayload(
    @SerialName("about_url") val aboutURL: SerializableURL,
    val name: String,
    val type: String,
    val url: SerializableURL,
    val vendor: String
)

@Serializable
data class PackagePayload(
    val id: Int,
    val name: String,
    val namespace: String,
    val description: String?,
    val ecosystem: String,
    @SerialName("package_type") val packageType: String,
    @SerialName("html_url") val htmlURL: SerializableURL,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    val owner: SenderPayload,
    @SerialName("package_version") val packageVersion: PackageVersionPayload,
    val registry: PackageRegistryPayload
)

/**
 * Represents the body of a package event payload
 * https://docs.github.com/en/developers/webhooks-and-events/webhooks/webhook-events-and-payloads#package
 */
@Serializable
data class PackageEventPayload(
    override val action: String,
    override val sender: SenderPayload,
    override val repository: RepositoryPayload,
    @SerialName("package") val pkg: PackagePayload,
    @Transient override val organization: OrganizationPayload? = null,
    @Transient override val installation: InstallationPayload? = null
) : EventPayload()
