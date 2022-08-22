package opstopus.deploptopus.github.events

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import opstopus.deploptopus.NotFound
import kotlin.reflect.KClass

internal object EventTypeSerializer : KSerializer<EventType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "EventType",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: EventType) {
        encoder.encodeString(value.eventName)
    }

    override fun deserialize(decoder: Decoder): EventType {
        return EventType[decoder.decodeString()]
    }
}

/**
 * Represents a mapping of event names to their expected payloads
 */
@Serializable(with = EventTypeSerializer::class)
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
