# 🛡️ File Guardian

File Guardian es una herramienta de línea de comandos escrita en Kotlin que vigila carpetas en tiempo real y permite recuperar archivos eliminados accidentalmente.

## Funcionalidades

-  Vigilancia en tiempo real de una carpeta
-  Backup automático de archivos antes de que sean eliminados
-  Detección instantánea de archivos borrados
-  Restauración de archivos desde el vault
-  Listado de todos los backups disponibles

##  Tecnologías

- Kotlin
- Gradle
- Java NIO (WatchService API)

## ▶️ Cómo ejecutar

### Requisitos
- JDK 21 o superior
- IntelliJ IDEA (recomendado)

### Pasos
1. Clona el repositorio:
   git clone https://github.com/ElySaez/file-guardian.git
2. Abre el proyecto en IntelliJ IDEA
3. Ejecuta `Main.kt`
4. Ingresa la carpeta que deseas vigilar

## 📖 Uso

Al ejecutar el programa se mostrará un menú con las siguientes opciones:

Hacer backup de todos los archivos
Iniciar vigilancia de la carpeta
Ver archivos en el vault
Restaurar un archivo
Salir


## 📂 Estructura del proyecto

    file-guardian/
    ├── src/
    │   └── main/
    │       └── kotlin/
    │           ├── Main.kt         # Menú principal
    │           ├── Vault.kt        # Gestión de backups
    │           └── FileWatcher.kt  # Vigilancia de carpetas
    └── build.gradle.kts

## 📝 Licencia

MIT
