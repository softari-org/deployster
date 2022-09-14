package opstopus.deploptopus

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGABRT
import platform.posix.SIGINT
import platform.posix.SIGKILL
import platform.posix.SIGSEGV
import platform.posix.atexit
import platform.posix.signal

/**
 * Used to manage lifecycle of anything that has a lifecycle that spans the entire program.
 *
 * GlobalLifecycle.end should be the last thing called in the main function.
 */
object GlobalLifecycle {
    private val destructors = mutableListOf<() -> Unit>()
    private val logger = KtorSimpleLogger("GlobalLifecycle")

    init {
        atexit(staticCFunction<Unit> { GlobalLifecycle.end() })
        for (sig in listOf(SIGINT, SIGKILL, SIGSEGV, SIGABRT)) {
            signal(sig, staticCFunction { _: Int -> GlobalLifecycle.end() })
        }
    }

    fun manage(destructor: () -> Unit) {
        this.destructors.add(destructor)
    }

    fun end() {
        this.logger.info("Destroying global objects")
        for (destructor in this.destructors) {
            this.logger.debug("Calling destructor $destructor")
            destructor()
        }
        this.destructors.clear()
    }
}
