package opstopus.deploptopus.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

val jsonFormatter = Json {
    ignoreUnknownKeys = true
    @OptIn(ExperimentalSerializationApi::class)
    explicitNulls = false
}
