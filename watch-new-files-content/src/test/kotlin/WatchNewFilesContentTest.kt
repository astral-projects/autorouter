import org.junit.jupiter.api.Test
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

class WatchNewFilesContentTest {

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

    @Test
    fun `Concurrent Test`() {
        val dir = Paths.get("src/test/kotlin/watchable-dir")





        val list = mutableListOf<String>()
        val watcherThread = thread {
            for (fileContent in dir.watchNewFilesContent()) {
                for (line in fileContent) {
                    list.add(line)
                }
            }
        }
        Thread.sleep(100)

        val fileName = "ola.txt"
        val fileText = "Então ze sigura te "
        File(fileName).writeText(fileText)

        // Give the watcher a chance to detect the change
        Thread.sleep(100)
        File("src/test/kotlin/watchable-dir/ola.txt").forEachLine { realLine ->
            list.forEach { line ->
                println("$line = $realLine")
                assertEquals(realLine, line)
            }
        }
        println("UI")
        watcherThread.interrupt()
    }


    @Test
    fun `step-by-step `(){
        val dir = Paths.get("src/test/kotlin/watchable-dir")
        val fileName = File(dir.toFile(),"ola.txt")
        val fileName2 = File(dir.toFile(),"ola2.txt")
        val sequence= dir.watchNewFilesContent()
        val sq: Sequence<String> =sequence.iterator().next()
        Paths.get("src/test/kotlin/watchable-dir/ola.txt").writeText("ola")
        println(sq.first())
    }
}