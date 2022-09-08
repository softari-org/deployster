package opstopus.deploptopus.system.runner

import opstopus.deploptopus.InternalServerError
import opstopus.deploptopus.system.FileIO
import platform.posix.EXIT_SUCCESS
import platform.posix.pclose
import platform.posix.popen

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
    ): String {
        val invocation = "ssh -i $key -l $user $host -p $port $command 2>&1"
        val io = FileIO(
            popen(invocation, "r")
                ?: throw InternalServerError("Failed to open process for execution."),
            closeWith = { pclose(it) }
        )

        // Get the output from our command, and check if it succeeded
        val output = io.read()
        val exitStatus = io.close()

        if (exitStatus != EXIT_SUCCESS) {
            throw InternalServerError(
                "Deployment process failed with exit code $exitStatus: $output"
            )
        }

        return output
    }
}
