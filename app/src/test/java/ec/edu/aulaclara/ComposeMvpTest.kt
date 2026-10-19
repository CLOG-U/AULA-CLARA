package ec.edu.aulaclara

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Headless Android framework validation when a hardware-accelerated emulator is unavailable. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ComposeMvpTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    private fun demo() {
        ui.waitUntil(30_000) { ui.onAllNodesWithTag("login_demo").fetchSemanticsNodes().isNotEmpty() }
        click("login_demo")
        ui.onNodeWithText("Hola, Carlos").assertIsDisplayed()
    }
    private fun click(tag: String) = ui.onNodeWithTag(tag).performScrollTo().performClick()
    private fun capture(name: String) {
        ui.waitForIdle()
        val view = ui.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        // Draw the actual measured Android view with native Skia. PixelCopy's hardware
        // redraw callback is not dispatched by Robolectric's headless window.
        ui.runOnIdle { view.draw(Canvas(bitmap)) }
        val colors = mutableSetOf<Int>()
        for (x in 0 until bitmap.width step 12) for (y in 0 until bitmap.height step 12) colors.add(bitmap.getPixel(x, y))
        assertTrue("The Android view must render content, not a blank frame", colors.size > 8)
        val directory = File("build/outputs/robolectric").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test fun createEditCompleteDeleteAndCapture() {
        demo()
        capture("01-inicio")
        click("new_delivery")
        ui.onNodeWithTag("delivery_title").performTextInput("Prototipo de navegación")
        ui.onNodeWithTag("delivery_notes").performScrollTo().performTextInput("Revisar pantallas y comprobar todos los recorridos.")
        ui.onNodeWithTag("delivery_title").performScrollTo()
        capture("02-formulario")
        click("save_delivery")
        ui.onNodeWithText("Prototipo de navegación").assertIsDisplayed()
        click("edit_delivery")
        ui.onNodeWithTag("delivery_title").performTextReplacement("Prototipo de Aula Clara")
        click("save_delivery")
        click("toggle_completed")
        ui.onNodeWithText("Completada", useUnmergedTree = true).assertIsDisplayed()
        capture("03-detalle-completado")
        click("delete_delivery")
        ui.onNodeWithText("Cancelar").performClick()
        ui.onNodeWithText("Prototipo de Aula Clara").assertExists()
        click("delete_delivery")
        ui.onAllNodesWithText("Eliminar entrega").onLast().performClick()
        ui.onNodeWithText("Hola, Carlos").assertIsDisplayed()
        ui.onNodeWithText("Prototipo de Aula Clara").assertDoesNotExist()
    }

    @Test fun catalogsFilterAndThemeNavigation() {
        demo()
        ui.onNodeWithTag("nav_catalog").performClick()
        ui.onNodeWithText("Diseño de apps").performClick()
        ui.onNodeWithTag("delete_catalog").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithContentDescription("Volver").performClick()
        click("new_subject")
        ui.onNodeWithTag("catalog_name").performTextInput("Redes")
        click("save_catalog")
        ui.onNodeWithText("Redes").assertExists()
        ui.onNodeWithTag("types_tab").performClick()
        ui.onNodeWithText("Proyecto").performClick()
        ui.onNodeWithTag("delete_catalog").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithContentDescription("Volver").performClick()
        ui.onNodeWithText("Nuevo tipo").performScrollTo().performClick()
        ui.onNodeWithTag("catalog_name").performTextInput("Laboratorio")
        click("save_catalog")
        ui.onNodeWithText("Laboratorio").performScrollTo().performClick()
        click("delete_catalog")
        ui.onAllNodesWithText("Eliminar tipo").onLast().performClick()
        ui.onNodeWithText("Laboratorio").assertDoesNotExist()
        ui.onNodeWithTag("nav_home").performClick()
        ui.onNodeWithText("Ver todas").performScrollTo().performClick()
        ui.onNodeWithTag("delivery_search").performTextInput("unidad")
        ui.onNodeWithText("Lectura unidad 3").assertExists()
        ui.onNodeWithText("Maquetado móvil").assertDoesNotExist()
        ui.onNodeWithContentDescription("Volver").performClick()
        ui.onNodeWithTag("nav_settings").performClick()
        ui.onNodeWithText("Oscuro").performClick()
        ui.onNodeWithText("Claro").performClick()
        ui.onNodeWithTag("nav_home").performClick()
        ui.onNodeWithText("Hola, Carlos").assertIsDisplayed()
    }

    @Test fun formValidationAndDiscardKeepDraft() {
        demo()
        click("new_delivery")
        click("save_delivery")
        ui.onNodeWithText("Escribe un título para continuar.").assertExists()
        ui.onNodeWithTag("delivery_title").performScrollTo().performTextInput("Borrador")
        ui.onNodeWithContentDescription("Volver").performClick()
        ui.onNodeWithText("¿Descartar cambios?").assertIsDisplayed()
        ui.onNodeWithText("Seguir editando").performClick()
        ui.onNodeWithTag("delivery_title").assertTextContains("Borrador")
        click("date_selector")
        ui.onNodeWithText("Confirmar fecha").performClick()
        click("time_selector")
        ui.onNodeWithText("Confirmar hora").performClick()
    }

    @Test fun newSessionCreatesFirstSubjectWithoutLosingDeliveryDraft() {
        ui.waitUntil(30_000) { ui.onAllNodesWithTag("auth_submit").fetchSemanticsNodes().isNotEmpty() }
        click("auth_submit")
        ui.onNodeWithText("Escribe un correo válido, por ejemplo nombre@correo.com.").assertExists()
        ui.onNodeWithTag("auth_email").performTextInput("ana@ejemplo.com")
        ui.onNodeWithTag("auth_password").performTextInput("Ejemplo2026")
        click("auth_submit")
        ui.onNodeWithText("Crear primera entrega").performScrollTo().performClick()
        ui.onNodeWithTag("delivery_title").performTextInput("Primera actividad")
        ui.onNodeWithText("Crear primera materia").performScrollTo().performClick()
        ui.onNodeWithTag("catalog_name").performTextInput("Programación")
        click("save_catalog")
        ui.onNodeWithTag("delivery_title").assertTextContains("Primera actividad")
        click("save_delivery")
        ui.onNodeWithText("Primera actividad").assertIsDisplayed()
    }

    @Test @Config(sdk = [34], qualifiers = "w320dp-h568dp-xhdpi")
    fun compactScreenKeepsPrimaryActionsReachable() {
        demo()
        capture("adaptable-320dp")
        click("new_delivery")
        ui.onNodeWithTag("delivery_title").performTextInput("Pantalla pequeña")
        click("save_delivery")
        ui.onNodeWithText("Pantalla pequeña").assertIsDisplayed()
    }

    @Test @Config(sdk = [34], qualifiers = "w800dp-h1280dp-xhdpi")
    fun tabletScreenAndActivityRecreationKeepAgenda() {
        demo()
        capture("adaptable-800dp")
        ui.activityRule.scenario.recreate()
        ui.waitUntil(30_000) { ui.onAllNodesWithText("Hola, Carlos").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Maquetado móvil").assertExists()
    }
}
