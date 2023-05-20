import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

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
}