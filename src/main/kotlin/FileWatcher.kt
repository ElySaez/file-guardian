import java.io.File
import java.nio.file.*

class FileWatcher(private val watchPath: String, private val vault: Vault) {

    private val watchDir = File(watchPath)
    var onEvent: ((String) -> Unit)? = null

    fun start() {
        if (!watchDir.exists()) {
            onEvent?.invoke("La carpeta no existe: $watchPath")
            return
        }

        val watchService = FileSystems.getDefault().newWatchService()
        watchDir.toPath().register(
            watchService,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )

        onEvent?.invoke("Vigilando: ${watchDir.absolutePath}")

        while (true) {
            val key = watchService.take()
            for (event in key.pollEvents()) {
                val fileName = event.context().toString()
                val tipo = when (event.kind()) {
                    StandardWatchEventKinds.ENTRY_DELETE -> "🗑️ Eliminado"
                    StandardWatchEventKinds.ENTRY_CREATE -> "✅ Creado"
                    StandardWatchEventKinds.ENTRY_MODIFY -> "✏️ Modificado"
                    else -> "Evento"
                }
                onEvent?.invoke("$tipo: $fileName")
            }
            key.reset()
        }
    }

    fun backupAll() {
        val files = watchDir.listFiles() ?: return
        var count = 0
        for (file in files) {
            if (file.isFile) {
                vault.backup(file)
                count++
            }
        }
        onEvent?.invoke("$count archivos respaldados.")
    }
}