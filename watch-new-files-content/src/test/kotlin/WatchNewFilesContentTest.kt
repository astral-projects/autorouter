import org.junit.jupiter.api.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WatchNewFilesContentTest {

    private val defaultTextA = "testStringA"
    private val defaultTextB = "testStringB"
    private val defaultTextC = "testStringC"

    private lateinit var dir: Path
    private lateinit var fileA: Path
    private lateinit var fileB: Path
    private lateinit var students: Path

    @BeforeEach
    fun setup() {
        val directoryName = "watchable-dir"
        dir = Files.createDirectory(Paths.get("src/test/kotlin/$directoryName"))
        fileA = Files.createFile(dir.resolve("A.txt"))
        fileB = Files.createFile(dir.resolve("B.txt"))
        students = Files.createFile(dir.resolve("students.txt"))
        students.writeText(
            List(10) {
                "Student $it"
            }.toList().joinToString("\n")
        )
    }

    @AfterEach
    fun dispose() {
        Files.deleteIfExists(fileA)
        Files.deleteIfExists(fileB)
        Files.deleteIfExists(students)
        Files.deleteIfExists(dir)
    }

    // try-out-tests
    // @Test
    fun `Watch a directory lazily`() {
        for (fileContent in dir.watchNewFilesContent()) {
            for (line in fileContent) {
                println(line)
            }
        }
    }

    @Test
    fun `Watch a directory eagerly`() {
        Files.newBufferedReader(students).use { reader ->
            // next() and hasNext() logic here
            while (true) {
                val line = reader.readLine() ?: break
                println(line)
            }
        }
    }

    // concurrent-tests
    @Test
    fun `Watch a directory lazily concurrently with one file modified only`() {
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
        // ensure
        Thread.sleep(2000)
        // write to file
        fileA.writeText(defaultTextA)
        // wait for new file has been watched by threadWatchFile
        println("Waiting for threadWatchFile to finish")
        threadWatchFile.join()
        val iter = lines.iterator()
        assertTrue { iter.hasNext() }
        assertEquals(defaultTextA, iter.next())
    }

    @Test
    fun `Watch a directory lazily concurrently with more than one file modified`() {
        val sequence= dir.watchNewFilesContent()
        val latch = CountDownLatch(1)
        lateinit var linesA: Sequence<String>
        val threadAWatchFile = thread {
            // signal that threadWatchFile is ready to watch
            latch.countDown()
            // start watching and blocking until new file created or modified
            linesA = sequence.iterator().next()
        }
        // wait for threadWatchFile start watching
        latch.await()
        // ensure
        Thread.sleep(2000)
        // write to file
        fileA.writeText(defaultTextA)
        threadAWatchFile.join()
        val latchB = CountDownLatch(1)
        lateinit var linesB: Sequence<String>
        val threadBWatchFile = thread {
            // signal that threadWatchFile is ready to watch
            latchB.countDown()
            // start watching and blocking until new file created or modified
            linesB = sequence.iterator().next()
        }
        latchB.await()
        Thread.sleep(2000)
        // write to file
        fileB.writeText(defaultTextB)
        threadBWatchFile.join()
        // A-Assertions
        val iterA = linesA.iterator()
        assertTrue { iterA.hasNext() }
        assertEquals(defaultTextA, iterA.next())
        assertFalse { iterA.hasNext() }
        // B-Assertions
        val iterB = linesB.iterator()
        assertTrue { iterB.hasNext() }
        assertEquals(defaultTextB, iterB.next())
        assertFalse { iterB.hasNext() }
    }

    @Test
    fun `Watch a directory lazily concurrently with one file modified , writting before the sequence is executted and writting after the execute `() {
        val sequence= dir.watchNewFilesContent()
        fileA.writeText(defaultTextA)
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
        // ensure that threadWatchFile is watching before write to file
        Thread.sleep(2000)
        // write to file
        fileA.writeText(defaultTextC)
        threadWatchFile.join()
        // A-Assertions
        val iter = lines.iterator()
        assertTrue { iter.hasNext() }
        assertEquals(defaultTextC, iter.next())
        assertFalse { iter.hasNext() }
    }
}