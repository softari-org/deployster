package opstopus.deploptopus

import kotlinx.serialization.Serializable

@Serializable
data class Status(val version: String) {
    companion object {
        const val version = "1.0-SNAPSHOT"
        fun get(): Status {
            return Status("deploptopus version ${Status.version}")
        }
    }
}
