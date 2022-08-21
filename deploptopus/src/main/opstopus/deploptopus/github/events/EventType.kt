package opstopus.deploptopus.github.events

import opstopus.deploptopus.NotFound
import kotlin.reflect.KClass

/**
 * Represents a mapping of event names to their expected payloads
 */
enum class EventType(val eventName: String, val payloadType: KClass<out EventPayload>) {
    PACKAGE("package", PackageEventPayload::class);

    companion object {
        /**
         * Get an event type by the name passed through the X-GitHub-Event header
         */
        operator fun get(name: String) = values().find { it.eventName == name }
            ?: throw NotFound("Unsupported event type: $name")
    }
}
