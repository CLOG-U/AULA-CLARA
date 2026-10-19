# Guía de explicación y revisión

## Qué problema resuelve

Aula Clara reúne entregas dispersas entre aula virtual y mensajes. Permite identificar próximos vencimientos y registrar una actividad con materia, tipo, prioridad y notas.

## Cómo explicar el código

1. **MainActivity**: punto de entrada de Android, abre la interfaz Compose.
2. **AulaClaraApp**: crea estado, tema y NavController; declara destinos y barra inferior.
3. **AgendaState**: listas observables y preferencias de la demostración. `rememberSaveable` y un Saver restauran la sesión al recrear la actividad; no hay base de datos.
4. **AgendaRules**: búsqueda, filtros, orden, conteo semanal y comprobación de relaciones antes de borrar.
5. **Screens**: reciben estado y funciones de navegación; formularios con validación y confirmaciones.
6. **Theme / Components**: paleta, tipografía, formas y controles compartidos Material 3.
7. **Ícono**: libro con marca de verificación, vector y recursos adaptativos para el launcher.

## Recorrido sugerido para la revisión

Explorar demostración → Inicio → Nueva entrega → Guardar → Editar → Completar → Ver todas → Filtrar → Materias → intentar borrar una materia asociada → crear y borrar una materia libre → Ajustes → cambiar tema → cerrar sesión.

Después iniciar una sesión vacía y demostrar que crear la primera materia no pierde el borrador de una entrega.

## Preguntas habituales

**¿Por qué no Room ni ViewModel?** La consigna de esta etapa se centra en pantallas y navegación y no pide presentar esos elementos de arquitectura. Se reservan para la implementación posterior.

**¿El acceso es real?** No. El formulario comprueba formato, longitud y confirmación; abre una sesión de demostración. No verifica identidad ni guarda contraseñas.

**¿Los avisos funcionan en segundo plano?** No. En esta etapa se demuestra la preferencia visual de 24 horas. La programación y los permisos se implementarán después.

**¿Cómo se evita borrar datos relacionados?** Se cuentan las entregas asociadas, se deshabilita la acción y las funciones de borrado vuelven a comprobar la regla.

**¿Se guardan los datos?** Durante la sesión y recreación de actividad. No se garantiza almacenamiento permanente tras cerrar/forzar la app. Cerrar sesión limpia el contenido.

**¿Qué significa cada pantalla P01–P20?** Algunas son destinos y otras estados o diálogos. La tabla de trazabilidad explica dónde encontrar cada una.

## Responsabilidades de entrega

- Confirmar los integrantes reales; no atribuir participación que no ocurrió.
- Cada integrante debe registrar su propia contribución en Git y entenderla.
- Revisar el Word unificado, su repositorio y las capturas.
- Cada estudiante sube su Word a Moodle en la fecha indicada en el aula virtual.
- El código preparado y las capturas no sustituyen entrevistas de usuarios; estas no se han realizado como parte de esta implementación.
