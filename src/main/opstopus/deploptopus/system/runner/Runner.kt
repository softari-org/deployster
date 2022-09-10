package opstopus.deploptopus.system.runner

import kotlinx.serialization.Serializable
import opstopus.deploptopus.InternalServerError
import opstopus.deploptopus.system.FileIO
import platform.posix.pclose
import platform.posix.popen

@Serializable
data class RunnerIO(val status: Int, val output: String)

/**
 * Runs deployment tasks
 */
object Runner {
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
}
