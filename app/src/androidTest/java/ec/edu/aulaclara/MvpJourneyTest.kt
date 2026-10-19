package ec.edu.aulaclara

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MvpJourneyTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    private fun demo() {
        ui.waitUntil(30_000) { ui.onAllNodesWithTag("login_demo").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("login_demo").performScrollTo().performClick()
        ui.onNodeWithText("Hola, Carlos").assertIsDisplayed()
    }
    private fun clickTag(tag: String) = ui.onNodeWithTag(tag).performScrollTo().performClick()
    private fun capture(name: String) {
        ui.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        assertTrue("Screenshot must exist", bitmap != null)
        val dir = File(instrumentation.targetContext.getExternalFilesDir(null), "evidencias").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test fun createEditCompleteAndDeleteDelivery() {
        demo()
        capture("01-inicio")
        clickTag("new_delivery")
        ui.onNodeWithTag("delivery_title").performTextInput("Prototipo de navegación")
        ui.onNodeWithTag("delivery_notes").performScrollTo().performTextInput("Revisar pantallas y comprobar todos los recorridos.")
        ui.activityRule.scenario.onActivity { activity ->
            val imm = activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
        }
        ui.onNodeWithTag("delivery_title").performScrollTo()
        capture("02-formulario")
        clickTag("save_delivery")
        ui.onNodeWithText("Prototipo de navegación").assertIsDisplayed()
        clickTag("edit_delivery")
        ui.onNodeWithTag("delivery_title").performTextReplacement("Prototipo de Aula Clara")
        clickTag("save_delivery")
        ui.onNodeWithText("Detalle de entrega").assertIsDisplayed()
        ui.onNodeWithText("Prototipo de Aula Clara").performScrollTo().assertIsDisplayed()
        clickTag("toggle_completed")
        ui.onNodeWithText("Prototipo de Aula Clara").performScrollTo()
        ui.onNodeWithText("Completada", useUnmergedTree = true).assertIsDisplayed()
        capture("03-detalle-completado")
        clickTag("delete_delivery")
        ui.onNodeWithText("Cancelar").performClick()
        ui.onNodeWithText("Prototipo de Aula Clara").assertExists()
        clickTag("delete_delivery")
        ui.onAllNodesWithText("Eliminar entrega").onLast().performClick()
        ui.onNodeWithText("Hola, Carlos").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Prototipo de Aula Clara").assertDoesNotExist()
    }

    @Test fun emptyAccountCanCreateFirstSubjectAndDelivery() {
        ui.waitUntil(30_000) { ui.onAllNodesWithText("Crear cuenta").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        ui.onNodeWithTag("register_name").performTextInput("Ana Pérez")
        ui.onNodeWithTag("auth_email").performTextInput("ana@ejemplo.com")
        ui.onNodeWithTag("auth_password").performTextInput("Ejemplo2026")
        ui.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput("Ejemplo2026")
        clickTag("auth_submit")
        ui.onNodeWithText("Crear primera entrega").performScrollTo().performClick()
        ui.onNodeWithTag("delivery_title").performTextInput("Primera actividad")
        ui.onNodeWithText("Crear primera materia").performScrollTo().performClick()
        ui.onNodeWithTag("catalog_name").performTextInput("Programación")
        clickTag("save_catalog")
        ui.onNodeWithTag("delivery_title").assertTextContains("Primera actividad")
        clickTag("save_delivery")
        ui.onNodeWithText("Primera actividad").assertIsDisplayed()
    }

    @Test fun linkedSubjectIsProtectedAndUnusedSubjectCanBeDeleted() {
        demo()
        ui.onNodeWithTag("nav_catalog").performClick()
        ui.onNodeWithText("Diseño de apps").performClick()
        ui.onNodeWithTag("delete_catalog").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithContentDescription("Volver").performClick()
        clickTag("new_subject")
        ui.onNodeWithTag("catalog_name").performTextInput("Materia temporal")
        clickTag("save_catalog")
        ui.onNodeWithText("Materia temporal").performScrollTo().performClick()
        clickTag("delete_catalog")
        ui.onAllNodesWithText("Eliminar materia").onLast().performClick()
        ui.onNodeWithText("Materia temporal").assertDoesNotExist()
    }

    @Test fun filtersSearchAndDiscardAreReachable() {
        demo()
        ui.onNodeWithText("Ver todas").performScrollTo().performClick()
        ui.onNodeWithTag("delivery_search").performTextInput("unidad")
        ui.onNodeWithText("Lectura unidad 3").assertExists()
        ui.onNodeWithText("Maquetado móvil").assertDoesNotExist()
        clickTag("open_filters")
        ui.onNodeWithText("Completadas").performClick()
        ui.onNodeWithText("Aplicar filtros").performScrollTo().performClick()
        ui.onNodeWithText("No hay entregas que coincidan con tu búsqueda y filtros.").assertExists()
        ui.onNodeWithText("Mostrar todas").performClick()
        clickTag("new_delivery")
        ui.onNodeWithTag("delivery_title").performTextInput("Borrador sin guardar")
        ui.onNodeWithContentDescription("Volver").performClick()
        ui.onNodeWithText("¿Descartar cambios?").assertIsDisplayed()
        ui.onNodeWithText("Seguir editando").performClick()
        ui.onNodeWithTag("delivery_title").assertTextContains("Borrador sin guardar")
    }

    @Test fun settingsThemeReminderAndLogoutWork() {
        demo()
        ui.onNodeWithTag("nav_settings").performClick()
        ui.onNodeWithText("Oscuro").performClick()
        ui.onNodeWithText("Claro").performClick()
        clickTag("reminders_switch")
        ui.onNodeWithText("Avisos de demostración").assertIsDisplayed()
        ui.onNodeWithText("Entendido").performClick()
        clickTag("sign_out")
        ui.onAllNodesWithText("Cerrar sesión").onLast().performClick()
        ui.onNodeWithTag("login_demo").assertExists()
    }

    @Test fun activityRecreationKeepsSessionAndAgenda() {
        demo()
        ui.activityRule.scenario.recreate()
        ui.waitUntil(30_000) { ui.onAllNodesWithText("Hola, Carlos").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Maquetado móvil").assertExists()
    }
}
