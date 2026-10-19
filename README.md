# Aula Clara · Etapa 2

Agenda académica Android nativa: pantallas y navegación del prototipo de la etapa 1, implementadas en **Kotlin, Jetpack Compose, Material 3 y Navigation Compose**.

Trabajo autónomo individual de Carlos Luis Ortiz García, para Desarrollo de aplicaciones móviles nativas.

## Abrir y ejecutar

1. Abrir esta carpeta con Android Studio y esperar la sincronización de Gradle.
2. Usar JDK 17 (Gradle JDK) e instalar Android SDK Platform 35 y Build Tools 35.0.0.
3. Iniciar un emulador o conectar un dispositivo con Android 8.0 / API 26 o posterior.
4. Ejecutar la configuración `app`.

El SDK local se configura mediante Android Studio o un `local.properties` con `sdk.dir=...`; este archivo no se versiona.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

El APK de desarrollo se genera en `app/build/outputs/apk/debug/app-debug.apk`. Está firmado con la clave de depuración, para instalación y evaluación académica; no es una publicación en Google Play.

## Recorrido de demostración

1. Pulsar **Explorar demostración** para entrar como Carlos con tres entregas de ejemplo y cuatro materias. Las fechas se calculan respecto al día de ejecución.
2. En Inicio, abrir una entrega o pulsar Nueva entrega.
3. Crear una actividad, editarla, marcarla completada y volver a marcarla pendiente.
4. Abrir Ver todas y combinar búsqueda por título/materia/tipo/notas, filtro por estado y filtro por materia.
5. En Materias, administrar materias y tipos. El borrado se bloquea mientras tengan entregas asociadas, incluso completadas.
6. En Ajustes, probar tema claro/oscuro/sistema y orden de la lista. La opción de avisos solo muestra una preferencia visual.
7. Cerrar sesión para limpiar la agenda de demostración.

Crear cuenta o Iniciar sesión validan los formularios y abren una sesión vacía con tres tipos iniciales. **No verifican identidad ni crean cuentas reales.** No se almacenan contraseñas. Para la primera entrega, se puede crear una materia y regresar al formulario conservando el borrador.

## Alcance y límites

- Implementa las referencias P01–P20 mediante pantallas, variantes de estado, diálogos y paneles nativos; véase [trazabilidad](docs/TRAZABILIDAD.md).
- El estado reside en Compose y se conserva con `rememberSaveable` al recrear la actividad. Esto no sustituye almacenamiento permanente: cerrar sesión lo limpia y no se garantiza conservarlo después de cerrar o forzar la aplicación.
- Los formularios conservan valores ante errores y confirman el descarte de cambios. Borrar requiere confirmación.
- La semana es de lunes a domingo. El resumen cuenta pendientes de esa semana. Las pendientes de fechas pasadas se identifican como vencidas.
- Se permiten fechas pasadas para registrar pendientes existentes. El orden por defecto es vencimiento ascendente, incluidas las vencidas.
- Los nombres duplicados se comparan ignorando espacios repetidos, mayúsculas y tildes, dentro de la sesión.
- Los avisos de 24 horas son una **vista previa**, sin permisos ni notificaciones del sistema.
- Room, DataStore, ViewModel, backend y autenticación real se reservan para etapas posteriores. Esta entrega no necesita credenciales ni acceso a Internet en la aplicación.

## Organización del código

`MainActivity` abre `AulaClaraApp`, que contiene el `NavHost` y la barra inferior. `AgendaState` reúne el estado de demostración compartido; `AgendaRules` contiene filtros, orden y reglas verificables. Cada archivo `*Screen` implementa una parte del recorrido. `Theme` y `Components` reúnen el diseño común. No se implementa una capa de repositorios ni una fuente de datos.

Las interfaces se construyen en Compose. Los XML presentes corresponden al manifiesto, valores de plataforma y recursos vectoriales del ícono, no a layouts de pantallas.

## Diseño adaptable

Contenido de ancho flexible, límite de lectura de 840 dp, formularios desplazables, controles que fluyen a varias líneas, insets del sistema y espacio para el teclado. El selector de hora usa entrada compacta cuando falta ancho. Los colores se acompañan de nombres y las acciones de icono tienen descripciones.

## Evidencia y entrega

- [Inicio](docs/evidencias/01-inicio.png), [formulario](docs/evidencias/02-formulario.png) y [detalle completado](docs/evidencias/03-detalle-completado.png): interfaz ejecutada con Android API 34 y renderizado nativo de Robolectric.
- [Resultados de verificación](docs/evidencias/LEER.md), con informes JUnit y Android Lint.
- [Guía para explicar el proyecto](docs/GUIA_DEFENSA.md).
- [Trazabilidad de pantallas y requisitos](docs/TRAZABILIDAD.md).
- `docs/evidencias/`: capturas de la aplicación ejecutada y resumen de verificación, cuando se han generado.
- En el paquete completo, `Entrega/` contiene el Word, los PDF y el APK; `AulaClara/` contiene el proyecto Android. Estos archivos se generan fuera del checkout.

La rúbrica pide conservar el repositorio `https://github.com/CLOG-U/AULA-CLARA`, registrar la participación en Git y entregar personalmente el Word en Moodle.

## Entorno en la nube

La tarea ya está aislada: trabajar en este checkout, sin crear worktrees. `bash scripts/cloud_setup.sh` instala o verifica las herramientas oficiales y ejecuta compilación, pruebas JVM y Lint. No cambia código fuente ni versiones de dependencias. Las descargas de herramientas se verifican con las sumas publicadas por sus distribuidores.

En una sesión posterior, activar `source /workspace/.tools/env.sh`. Las herramientas y dependencias pueden conservarse en la instantánea; los procesos deben iniciarse otra vez. Para pruebas instrumentadas en el AVD dedicado:

```sh
scripts/start_emulator.sh > /workspace/scratch/aula-emulator.log 2>&1 &
scripts/prepare_emulator.sh
./gradlew :app:connectedDebugAndroidTest
```

Sin KVM, el arranque por software tarda varios minutos. La preparación ajusta el margen de espera del framework únicamente en el AVD de desarrollo, desactiva las radios para probar el flujo sin conexión y verifica `sys.boot_completed`. Las pruebas JVM de Compose permiten revisar los recorridos sin depender del arranque del emulador.
