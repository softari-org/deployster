package opstopus.deploptopus.github.events

import kotlinx.serialization.Serializable

@Serializable
data class SenderPayload

@Serializable
data class RepositoryPayload

@Serializable
data class OrganizationPayload

@Serializable
data class InstallationPayload

/**
 * Represents the common properties shared by every event payload. All event payload
 * data classes must implement this class.
 */
abstract class EventPayload {
    abstract val action: String
    abstract val sender: SenderPayload
    abstract val repository: RepositoryPayload
    abstract val organization: OrganizationPayload
    abstract val installation: InstallationPayload
}

data class PackageEventPayload(
    override val action: String,
    override val sender: SenderPayload,
    override val repository: RepositoryPayload,
    override val organization: OrganizationPayload,
    override val installation: InstallationPayload
) : EventPayload()
