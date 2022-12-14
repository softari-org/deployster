package opstopus.deploptopus.system.runner

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.Serializable
import opstopus.deploptopus.InternalServerError
import opstopus.deploptopus.system.FileIO
import platform.posix.EXIT_SUCCESS
import platform.posix.pclose
import platform.posix.popen

@Serializable
data class RunnerIO(val status: Int, val output: String)

/**
 * Runs deployment tasks
 */
object Runner {
    val logger = KtorSimpleLogger("Runner")

    /**
     * Run a command on a remote server
     */
    fun runRemote(
        user: String,
        host: String,
        port: UInt,
        key: String,
        command: String
    ): RunnerIO {
        // Ensure that the provided key is usable
        this.verifyHostKey(host, port)

        val invocation = "ssh -i $key -l $user $host -p $port $command 2>&1"
        val io = FileIO(
            popen(invocation, "r")
                ?: throw InternalServerError("Failed to open process for execution."),
            closeWith = { pclose(it) }
        )

        val output = io.read()
        val exitStatus = io.close()

        return RunnerIO(exitStatus, output)
    }

    /**
     * Manually performs host key verification in case this is the first time connecting to the host
     */
    private fun verifyHostKey(host: String, port: UInt) {
        val keyScannerIO =
            FileIO(
                popen("ssh-keyscan -t rsa -p $port $host >> ~/.ssh/known_hosts", "r")
                    ?: throw InternalServerError("Failed to open key scanner process."),
                closeWith = { pclose(it) }
            )

        val output = keyScannerIO.read()

        if (keyScannerIO.close() != EXIT_SUCCESS) {
            this.logger.error(output)
            throw InternalServerError("Failed to perform manual host key verification.")
        }
    }
}
