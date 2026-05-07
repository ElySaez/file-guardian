import java.io.File
import java.nio.file.*

class FileWatcher(private val watchPath: String, private val vault: Vault) {

    private val watchDir = File(watchPath)

    fun start() {
        if (!watchDir.exists()) {
            println("La carpeta no existe: $watchPath")
            return
        }

        println("Vigilando carpeta: ${watchDir.absolutePath}")
        println("Presiona Ctrl+C para detener...")

        val watchService = FileSystems.getDefault().newWatchService()
        watchDir.toPath().register(watchService, StandardWatchEventKinds.ENTRY_DELETE)

        while (true) {
            val key = watchService.take()
            for (event in key.pollEvents()) {
                val fileName = event.context().toString()
                val deletedFile = File(watchPath, fileName)
                println("Archivo eliminado detectado: $fileName")
                // El archivo ya fue borrado, pero si tenemos backup previo lo mostramos
                println("Revisa el vault para recuperarlo con la opcion 3 del menu")
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
        println("$count archivos respaldados.")
    }
}