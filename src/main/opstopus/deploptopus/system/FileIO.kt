package opstopus.deploptopus.system

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import platform.posix.FILE
import platform.posix.SEEK_END
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.rewind
import kotlin.native.internal.Cleaner
import kotlin.native.internal.createCleaner

/**
 * Any exceptions related to POSIX FileIO
 */
class FileIOException(override val message: String?, override val cause: Throwable?) :
    Exception(message, cause) {
    constructor(message: String?) : this(message, null)
}

/**
 * GlobalLifecycle manager for POSIX file pointers
 */
class FileIO(
    val file: CPointer<FILE>,
    private val closeWith: (CValuesRef<FILE>) -> Int = { fclose(it) }
) {

    val length: Long = -1L
        get() {
            if (field >= 0) {
                return field
            }

            rewind(this.file)
            if (fseek(this.file, 0L, SEEK_END) < 0) {
                throw FileIOException("Failed to read file length.")
            }

            val len = ftell(this.file)
            if (len < 0) {
                throw FileIOException("Failed to read file length.")
            }

            rewind(this.file)

            return len
        }

    @OptIn(ExperimentalStdlibApi::class)
    private var cleaner: Cleaner? =
        createCleaner(this.file) { if (!this.closed) this.closeWith(it) }
    private var closed = false

    constructor(path: String, mode: String = "r") : this(
        fopen(path, mode) ?: throw FileIOException("Failed to open file at $path")
    )

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

    fun close(): Int {
        val code = this.closeWith(this.file)
        this.closed = true
        this.cleaner = null
        return code
    }

    companion object {
        private val BUFFER_SIZE = (sizeOf<ByteVar>() * 1024).toInt()
    }
}
