import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Desktop
import java.io.File
import javax.imageio.ImageIO

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "File Guardian v1.0") {
        App()
    }
}

@Composable
@Preview
fun App() {
    var watchPath by remember { mutableStateOf("") }
    var eventos by remember { mutableStateOf(listOf<String>()) }
    var entries by remember { mutableStateOf(listOf<VaultEntry>()) }
    var isWatching by remember { mutableStateOf(false) }
    var previewText by remember { mutableStateOf<String?>(null) }
    var previewImage by remember { mutableStateOf<File?>(null) }
    val vault = remember { Vault() }

    // Preview de texto
    previewText?.let { text ->
        Dialog(onDismissRequest = { previewText = null }) {
            Card(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vista previa", fontSize = 16.sp, color = Color(0xFF1976D2))
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        item { Text(text, fontSize = 12.sp) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { previewText = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }

    // Preview de imagen
    previewImage?.let { imgFile ->
        Dialog(onDismissRequest = { previewImage = null }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Vista previa", fontSize = 16.sp, color = Color(0xFF1976D2))
                    Spacer(modifier = Modifier.height(8.dp))
                    val bitmap = remember(imgFile) {
                        ImageIO.read(imgFile)?.toComposeImageBitmap()
                    }
                    if (bitmap != null) {
                        Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(300.dp))
                    } else {
                        Text("No se pudo cargar la imagen.")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { previewImage = null }) { Text("Cerrar") }
                }
            }
        }
    }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {

            Text("🛡️ File Guardian", fontSize = 24.sp, color = Color(0xFF1976D2))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = watchPath,
                    onValueChange = { watchPath = it },
                    label = { Text("Carpeta a vigilar") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("ej: C:\\Users\\Ely\\Documents") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val chooser = javax.swing.JFileChooser().apply {
                        fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
                        dialogTitle = "Selecciona una carpeta"
                    }
                    if (chooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        watchPath = chooser.selectedFile.absolutePath
                    }
                }) {
                    Text("Explorar...")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (watchPath.isEmpty()) {
                        eventos = listOf("⚠️ Ingresa una carpeta primero.") + eventos
                        return@Button
                    }
                    val w = FileWatcher(watchPath, vault)
                    w.onEvent = { msg -> eventos = listOf(msg) + eventos }
                    w.backupAll()
                    entries = vault.listEntries()
                    eventos = listOf("💾 Backup completado!") + eventos
                }) { Text("Backup") }

                Button(onClick = {
                    entries = vault.listEntries()
                    eventos = listOf("📋 ${entries.size} archivos en el vault.") + eventos
                }) { Text("Ver vault") }

                Button(
                    onClick = {
                        if (watchPath.isEmpty()) {
                            eventos = listOf("⚠️ Ingresa una carpeta primero.") + eventos
                            return@Button
                        }
                        if (isWatching) {
                            eventos = listOf("⚠️ Ya estás vigilando esta carpeta.") + eventos
                            return@Button
                        }
                        val dir = File(watchPath)
                        val archivos = dir.listFiles()?.filter { it.isFile } ?: emptyList()
                        val porTipo = archivos.groupBy { it.extension.ifEmpty { "sin extensión" } }
                            .map { (ext, files) -> "  • .${ext}: ${files.size}" }
                            .joinToString("\n")
                        eventos = listOf("👁️ Vigilancia iniciada", "📁 Total: ${archivos.size} archivos", porTipo) + eventos
                        isWatching = true
                        val watcher = FileWatcher(watchPath, vault)
                        watcher.onEvent = { msg ->
                            eventos = listOf(msg) + eventos
                            entries = vault.listEntries()
                        }
                        Thread { watcher.start() }.start()
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isWatching) Color(0xFF757575) else Color(0xFF388E3C)
                    )
                ) { Text(if (isWatching) "Vigilando..." else "Vigilar", color = Color.White) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                // Eventos
                Column(modifier = Modifier.weight(1f)) {
                    Text("📡 Actividad en tiempo real:", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(modifier = Modifier.fillMaxSize(), elevation = 2.dp) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            items(eventos) { evento ->
                                Text(
                                    text = evento,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    color = when {
                                        evento.startsWith("🗑️") -> Color(0xFFD32F2F)
                                        evento.startsWith("✅") -> Color(0xFF388E3C)
                                        evento.startsWith("✏️") -> Color(0xFFF57C00)
                                        evento.startsWith("⚠️") -> Color(0xFFFF6F00)
                                        else -> Color.DarkGray
                                    }
                                )
                                Divider(color = Color.LightGray, thickness = 0.5.dp)
                            }
                        }
                    }
                }

                // Vault
                Column(modifier = Modifier.weight(1f)) {
                    Text("💾 Archivos en el vault:", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(modifier = Modifier.fillMaxSize(), elevation = 2.dp) {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp).fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(entries) { entry ->
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(
                                        entry.originalName,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(entry.date, fontSize = 10.sp, color = Color.Gray)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Botón Ver
                                        TextButton(
                                            onClick = {
                                                val file = vault.getFile(entry.vaultName)
                                                val ext = file.extension.lowercase()
                                                when {
                                                    ext in listOf("txt", "md", "csv", "log", "kt", "java") -> {
                                                        previewText = file.readText()
                                                    }
                                                    ext in listOf("jpg", "jpeg", "png", "gif", "bmp") -> {
                                                        previewImage = file
                                                    }
                                                    else -> {
                                                        Desktop.getDesktop().open(file)
                                                    }
                                                }
                                            },
                                            contentPadding = PaddingValues(4.dp)
                                        ) { Text("Ver", fontSize = 11.sp, color = Color(0xFF1976D2)) }

                                        // Botón Restaurar
                                        TextButton(
                                            onClick = {
                                                val dest = "C:\\Users\\Ely\\Desktop\\${entry.originalName}"
                                                val ok = vault.restore(entry.vaultName, dest)
                                                eventos = if (ok) {
                                                    listOf("♻️ Restaurado: ${entry.originalName}") + eventos
                                                } else {
                                                    listOf("⚠️ Error al restaurar: ${entry.originalName}") + eventos
                                                }
                                            },
                                            contentPadding = PaddingValues(4.dp)
                                        ) { Text("Restaurar", fontSize = 11.sp, color = Color(0xFF388E3C)) }
                                    }
                                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}