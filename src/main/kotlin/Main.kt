import java.io.File

fun main() {
    println("=================================")
    println("   File Guardian v1.0")
    println("=================================")

    print("Ingresa la carpeta a vigilar (ej: C:\\Users\\Ely\\Documents): ")
    val watchPath = readLine() ?: return

    val vault = Vault()
    val watcher = FileWatcher(watchPath, vault)

    while (true) {
        println("\n--- MENU ---")
        println("1. Hacer backup de todos los archivos")
        println("2. Iniciar vigilancia de la carpeta")
        println("3. Ver archivos en el vault")
        println("4. Restaurar un archivo")
        println("0. Salir")
        print("Opcion: ")

        when (readLine()?.trim()) {
            "1" -> watcher.backupAll()
            "2" -> watcher.start()
            "3" -> {
                val backups = vault.listBackups()
                if (backups.isEmpty()) {
                    println("El vault esta vacio.")
                } else {
                    println("\nArchivos en el vault:")
                    backups.forEachIndexed { i, file ->
                        println("${i + 1}. ${file.name}")
                    }
                }
            }
            "4" -> {
                val backups = vault.listBackups()
                if (backups.isEmpty()) {
                    println("El vault esta vacio.")
                } else {
                    println("\nArchivos disponibles:")
                    backups.forEachIndexed { i, file ->
                        println("${i + 1}. ${file.name}")
                    }
                    print("Nombre del archivo a restaurar: ")
                    val nombre = readLine() ?: continue
                    print("Destino (ej: C:\\Users\\Ely\\Desktop\\archivo.txt): ")
                    val destino = readLine() ?: continue
                    vault.restore(nombre, destino)
                }
            }
            "0" -> {
                println("Hasta luego!")
                return
            }
            else -> println("Opcion no valida.")
        }
    }
}