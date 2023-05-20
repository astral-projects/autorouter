import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchKey

/**
 * Registers a [WatchService](https://docs.oracle.com/javase/8/docs/api/java/nio/file/WatchService.html) to a given [Path] and returns a [Sequence] with the content of new or modified files.
 */
fun Path.watchNewFilesContent(): Sequence<Sequence<String>> = sequence {
    // create an observer for the current file system in order to monitor the directory for changes
    fileSystem.newWatchService().use { service ->
        // Register the path to the service and watch for events for each directory
        register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
        // Start the infinite polling loop
        while (true) {
            // One key for each directory
            val key: WatchKey = service.take()
            // Dequeueing events
            for (watchEvent in key.pollEvents()) {
                // Get the type of the event
                val event = watchEvent.kind()
                val filename = watchEvent.context() as Path
                // Get the child name
                val childName: Path = fileName.resolve(filename)
                when (event) {
                    OVERFLOW -> continue
                    ENTRY_CREATE -> yield(emptySequence())
                    ENTRY_MODIFY -> {
                        try {
                            if (Files.probeContentType(childName.fileName) == "text/plain") {
                                yield(sequence {
                                    Files.newBufferedReader(this@watchNewFilesContent.resolve(childName.fileName))
                                        .use { reader ->
                                            // next() and hasNext() logic here
                                            while (true) {
                                                val line = reader.readLine() ?: break
                                                yield(line)
                                            }
                                        }
                                })
                            }
                        } catch (ex: IOException) {
                            throw ex
                        }
                    }
                    ENTRY_DELETE -> yield(emptySequence())
                }
            }
            if (!key.reset()) {
                break // loop
            }
        }
    }
}