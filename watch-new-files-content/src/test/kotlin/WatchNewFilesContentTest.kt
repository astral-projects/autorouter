import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.FileAttribute
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread
import kotlin.io.path.createFile
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WatchNewFilesContentTest {

    private val defaultText = "testString"
    private val fileAName = "A.txt"
    private val fileBName = "B.txt"

    private lateinit var dir: Path
    private lateinit var fileA: Path
    private lateinit var fileB: Path

    @BeforeEach
    fun setup() {
        val directoryName = "watchable-dir"
        dir = Files.createDirectory(Paths.get("src/test/kotlin/$directoryName"))
        fileA = Files.createFile(dir.resolve(fileAName))
        fileB = Files.createFile(dir.resolve(fileBName))
    }

    @AfterEach
    fun dispose() {
        Files.deleteIfExists(fileA)
        Files.deleteIfExists(fileB)
        Files.deleteIfExists(dir)
    }

    // try-out-tests
    @Test
    fun `Watch a directory lazily`() {
        val dir = Paths.get("src/test/kotlin/watchable-dir")
        for (fileContent in dir.watchNewFilesContent()) {
            for (line in fileContent) {
                println(line)
            }
        }
    }

    @Test
    fun `Watch a directory eagerly`() {
        val dir = Paths.get("src/test/kotlin/watchable-dir")
        val child: Path = dir.resolve("students.txt")
        Files.newBufferedReader(child).use { reader ->
            // next() and hasNext() logic here
            while (true) {
                val line = reader.readLine() ?: break
                println(line)
            }
        }
    }

    // concurrent-tests
    @Test
    fun `step-by-step`() {
        val sequence= dir.watchNewFilesContent()
        val latch = CountDownLatch(1)
        lateinit var lines: Sequence<String>
        val threadWatchFile = thread {
            // signal that threadWatchFile is ready to watch
            latch.countDown()
            // start watching and blocking until new file created or modified
            lines = sequence.iterator().next()
        }
        // wait for threadWatchFile start watching
        latch.await()
        Thread.sleep(2000) // ensure threadWatchFile is watching
        // write to file
        fileA.writeText(defaultText)
        // wait for new file has been watched by threadWatchFile
        println("Waiting for threadWatchFile to finish")
        threadWatchFile.join()
        val iter = lines.iterator()
        assertTrue { iter.hasNext() }
        assertEquals(defaultText, iter.next())
    }
}