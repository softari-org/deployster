package opstopus.deploptopus

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.cinterop.toKString
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import opstopus.deploptopus.github.events.EventType
import opstopus.deploptopus.system.FileIO
import opstopus.deploptopus.system.FileIOException
import platform.posix.F_OK
import platform.posix.access
import platform.posix.getenv
import kotlin.system.exitProcess

@Serializable
data class RepositoryEvent(
    val event: EventType,
    val repository: String
)

/**
 * Represents a single trigger
 */
@Serializable
data class Trigger(
    val on: RepositoryEvent,
    val command: String,
    val user: String,
    val host: String,
    val port: UInt,
    val key: String
)

/**
 * Represents the configuration of this deploptopus.
 */
@Serializable
data class Config(val triggers: List<Trigger>) {
    @Transient
    val githubSecret: String = getenv("DEPLOPTOPUS_WEBHOOK_SECRET")?.toKString() ?: ""

    companion object {
        private val log = KtorSimpleLogger("Config")

        /**
         * Loads the configuration file into its data representation
         */
        fun loadConfiguration(args: Array<String>): Config {
            // Try to find the file path via the -f argument
            val configFilePosition = args.indexOf("-f") + 1
            val configFilePath = if (configFilePosition == 0) {
                "/etc/deploptopus/config.json"
            } else {
                args.getOrElse(configFilePosition) {
                    log.error("Passed '-f', but no config file location provided.")
                    exitProcess(1)
                }
            }

            // If config file doesn't exist, exit because we don't know how to handle requests
            if (access(configFilePath, F_OK) != 0) {
                log.error("Config file at $configFilePath doesn't exist.")
                exitProcess(1)
            }

            // Load the contents of the config into a String
            val configFile: FileIO
            try {
                configFile = FileIO(configFilePath)
            } catch (_: FileIOException) {
                log.error("Could not read config file at $configFilePath")
                exitProcess(1)
            }
            return Json.decodeFromString(configFile.read())
        }
    }
}
