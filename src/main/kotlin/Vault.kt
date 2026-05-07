import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Vault(private val vaultPath: String = ".file-guardian-vault") {

    private val vaultDir = File(vaultPath)

    init {
        if (!vaultDir.exists()) {
            vaultDir.mkdirs()
            println("Vault creado en: ${vaultDir.absolutePath}")
        }
    }

    fun backup(file: File): Boolean {
        return try {
            val timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val backupName = "${file.nameWithoutExtension}_$timestamp.${file.extension}"
            val backupFile = File(vaultDir, backupName)
            Files.copy(file.toPath(), backupFile.toPath())
            println("Backup guardado: $backupName")
            true
        } catch (e: Exception) {
            println("Error al hacer backup: ${e.message}")
            false
        }
    }

    fun listBackups(): List<File> {
        return vaultDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun restore(backupName: String, destination: String): Boolean {
        val backupFile = File(vaultDir, backupName)
        if (!backupFile.exists()) {
            println("Archivo no encontrado en el vault: $backupName")
            return false
        }
        return try {
            val destFile = File(destination)
            Files.copy(backupFile.toPath(), destFile.toPath())
            println("Archivo restaurado en: $destination")
            true
        } catch (e: Exception) {
            println("Error al restaurar: ${e.message}")
            false
        }
    }
}