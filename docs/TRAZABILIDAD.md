# Pantallas, rutas y requisitos

| Referencia | Implementación | Ruta o activación |
|---|---|---|
| P01 Apertura | SplashScreen | `splash` |
| P02 Acceso | AuthScreen | `login` |
| P03 Registro | AuthScreen, modo registro | `register` |
| P04 Inicio | HomeScreen, con entregas | `home` |
| P05 Lista | DeliveryListScreen | `deliveries` |
| P06 Nueva entrega | DeliveryFormScreen | `delivery/new` |
| P07 Detalle pendiente | DeliveryDetailScreen | `delivery/{id}` |
| P08 Editar entrega | DeliveryFormScreen, precargado | `delivery/edit/{id}` |
| P09 Confirmación de borrado | AlertDialog | Eliminar entrega / catálogo libre |
| P10 Materias | CatalogScreen, pestaña Materias | `catalog` |
| P11 Editar materia | CatalogEditorScreen | `subject/edit/{id}` |
| P12 Ajustes | SettingsScreen | `settings` |
| P13 Completada | DeliveryDetailScreen, variante completada | Acción Marcar completada |
| P14 Validación | Errores del formulario | Guardar datos incompletos |
| P15 Tipos | CatalogScreen, pestaña Tipos | Pestaña Tipos en `catalog` |
| P16 Inicio vacío | HomeScreen, sin entregas | Sesión nueva sin datos |
| P17 Nueva materia | CatalogEditorScreen | `subject/new` |
| P18 Editor de tipo | CatalogEditorScreen | `type/new`, `type/edit/{id}` |
| P19 Fecha | Material DatePickerDialog | Botón de fecha en crear/editar |
| P20 Filtros | Material ModalBottomSheet | Acción Filtrar entregas |

La navegación usa `rememberNavController`, `NavHost`, rutas explícitas y argumentos `Long` para los identificadores. La barra inferior solo aparece en destinos principales. Atrás vuelve al origen; los formularios modificados piden confirmar el descarte. Los paneles temporales usan componentes Material 3, sin crear destinos innecesarios.

## Requisitos de la etapa 1 en esta entrega

| Requisitos | Estado de implementación |
|---|---|
| RF01 | Apertura y selección del destino de acceso/inicio según la sesión. |
| RF02–RF03 | Formularios y sesión demostrativa. Base de usuarios y autenticación real pendientes para otra etapa. |
| RF04–RF05 | CRUD de materias y tipos en memoria, validación y confirmaciones. |
| RF06–RF11 | Crear, ordenar, consultar, editar, eliminar y completar/reabrir entregas en memoria. |
| RF12 | Filtros por materia/estado, combinables con búsqueda. |
| RF13 | Preferencias de sesión y recreación de actividad; persistencia con DataStore pendiente. |
| RF14 | Vista previa de preferencia de aviso de 24 horas. Programación real pendiente. |
| RF15 | Pendientes de lunes a domingo, excluyendo completadas. |
| RF16 | Campos obligatorios, correo, longitud/coincidencia de contraseñas y duplicados de catálogos. |
| RF17 | Protección en la interfaz y operaciones de estado al borrar catálogos asociados. |

## Ajustes respecto al prototipo

- Componentes y áreas del sistema de Android en lugar del marco visual tipo iOS del PDF.
- Tipos iniciales y creación de la primera materia desde el formulario, preservando el borrador.
- Búsqueda definida por título, materia, tipo y notas; término Completadas unificado.
- Ajustes del orden aplicados a la lista completa. El inicio mantiene próximos vencimientos.
- Aviso fijo de 24 horas, como RF14; no se añaden intervalos sin un requisito.
- Estado vencida derivado de la fecha y del estado pendiente, sin crear un tercer estado persistido.
- Acceso de demostración y límites de persistencia visibles para evitar presentar funciones pendientes como completas.
