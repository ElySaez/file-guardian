import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class VaultEntry(
    val originalName: String,
    val vaultName: String,
    val date: String
)

class Vault(private val vaultPath: String = ".file-guardian-vault") {

    private val vaultDir = File(vaultPath)
    private val indexFile = File(vaultDir, "vault-index.txt")
    private val entries = mutableListOf<VaultEntry>()

    init {
        if (!vaultDir.exists()) {
            vaultDir.mkdirs()
        }
        loadIndex()
    }

    private fun loadIndex() {
        entries.clear()
        if (!indexFile.exists()) return
        indexFile.readLines().forEach { line ->
            val parts = line.split("|")
            if (parts.size == 3) {
                entries.add(VaultEntry(parts[0], parts[1], parts[2]))
            }
        }
    }

    private fun saveIndex() {
        indexFile.writeText(entries.joinToString("\n") {
            "${it.originalName}|${it.vaultName}|${it.date}"
        })
    }

    fun backup(file: File): Boolean {
        return try {
            val timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val vaultName = "${file.nameWithoutExtension}_$timestamp.${file.extension}"
            val backupFile = File(vaultDir, vaultName)
            Files.copy(file.toPath(), backupFile.toPath())

            val date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            entries.add(0, VaultEntry(file.name, vaultName, date))
            saveIndex()
            true
        } catch (e: Exception) {
            println("Error al hacer backup: ${e.message}")
            false
        }
    }

    fun listEntries(): List<VaultEntry> = entries.toList()

    fun getFile(vaultName: String): File = File(vaultDir, vaultName)

    fun restore(vaultName: String, destination: String): Boolean {
        val backupFile = File(vaultDir, vaultName)
        if (!backupFile.exists()) return false
        return try {
            val destFile = File(destination)
            if (destFile.exists()) destFile.delete()
            Files.copy(backupFile.toPath(), destFile.toPath())
            true
        } catch (e: Exception) {
            println("Error al restaurar: ${e.message}")
            false
        }
    }
}