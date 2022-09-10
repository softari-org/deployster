package opstopus.deploptopus

import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.takeFrom
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object URLSerializer : KSerializer<Url> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "URL",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: Url) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Url {
        return URLBuilder().takeFrom(decoder.decodeString()).build()
    }
}

object ContentTypeSerializer : KSerializer<ContentType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "ContentType",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: ContentType) {
        encoder.encodeString(
            "${value.contentType.lowercase()}/${value.contentSubtype.lowercase()}"
        )
    }

    override fun deserialize(decoder: Decoder): ContentType {
        var decoded = decoder.decodeString()
        if (decoded == "json") {
            decoded = "application/json"
        } else if (decoded == "form") {
            decoded = "multipart/form-data"
        }
        return ContentType.parse(decoded)
    }
}
