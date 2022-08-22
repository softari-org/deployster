package opstopus.deploptopus.system

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import platform.posix.FILE
import platform.posix.SIGINT
import platform.posix.atexit
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.signal
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze
import kotlin.native.ref.WeakReference

/**
 * Tracks references to FileIO objects, and closes any open file pointers as their
 * containers are destroyed
 */
private object FileDestructor {
    private val atom = AtomicReference(
        mutableListOf<Triple<WeakReference<FileIO>, ((CValuesRef<FILE>) -> Int), CPointer<FILE>>>().freeze()
    )
    private val registry
        get() = this.atom.value
    private val log = KtorSimpleLogger("FileDestructor")

    /**
     * Register exit deconstructors
     */
    init {
        atexit(staticCFunction<Unit> { FileDestructor.destroy() })
        signal(SIGINT, staticCFunction<Int, Unit> { FileDestructor.destroy() })
    }

    /**
     * Track the lifetime of a FileIO object and close it's file when it is collected
     */
    fun register(file: FileIO) {
        this.registry.add(Triple(WeakReference(file), file.closeWith, file.file))
    }

    /**
     * Close any files associated with dead references
     */
    fun clean() {
        val destroyed = this.registry.filter { it.first.get() == null }
        destroyed.forEach {
            this.log.debug("Closing file ${it.third.rawValue}")
            it.second(it.third)
        }
        this.registry.removeAll(destroyed)
    }

    /**
     * Close all files, regardless of the state of their reference
     */
    fun destroy() {
        this.log.debug("Destroying files")
        this.registry.forEach {
            this.log.debug("Closing file ${it.third.rawValue}")
            it.second(it.third)
        }
    }
}

/**
 * Any exceptions related to POSIX FileIO
 */
class FileIOException(override val message: String?, override val cause: Throwable?) :
    Exception(message, cause) {
    constructor(message: String?) : this(message, null)
}

/**
 * Lifecycle manager for POSIX file pointers
 */
class FileIO(
    internal val file: CPointer<FILE>,
    internal val closeWith: (CValuesRef<FILE>) -> Int = { fclose(it) }
) {
    constructor(path: String, mode: String = "r") : this(
        fopen(path, mode) ?: throw FileIOException("Failed to open file at $path")
    )

    init {
        FileDestructor.register(this)
        FileDestructor.clean()
    }

    fun read(): String {
        return buildString {
            memScoped {
                val buffer = this.allocArray<ByteVar>(FileIO.BUFFER_SIZE)
                var line: String? = ""
                do {
                    this@buildString.append(line)
                    line = fgets(
                        buffer,
                        FileIO.BUFFER_SIZE,
                        this@FileIO.file
                    )?.toKString()
                } while (line != null)
            }
        }
    }

    companion object {
        private val BUFFER_SIZE = (sizeOf<ByteVar>() * 1024).toInt()
    }
}
