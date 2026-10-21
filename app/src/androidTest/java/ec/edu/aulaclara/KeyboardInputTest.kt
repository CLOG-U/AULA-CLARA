package ec.edu.aulaclara

import android.view.KeyEvent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardInputTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    private fun type(tag: String, text: String) {
        ui.onNodeWithTag(tag).performScrollTo().performClick()
        ui.waitForIdle()
        ui.waitUntil(30_000) { ui.activity.hasWindowFocus() }
        // Dispatch Android key events, rather than Compose's SetText semantics.
        InstrumentationRegistry.getInstrumentation().sendStringSync(text)
        ui.waitForIdle()
        ui.onNodeWithTag(tag).assertTextContains(text)
    }

    @Test fun keyboardTypesEditsAndSubmitsForms() {
        ui.waitUntil(30_000) { ui.onAllNodesWithTag("auth_email").fetchSemanticsNodes().isNotEmpty() }
        type("auth_email", "teclado@ejemplo.comx")
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DEL)
        ui.onNodeWithTag("auth_email").assertTextContains("teclado@ejemplo.com")
        ui.onNodeWithContentDescription("Mostrar contraseña").performClick()
        type("auth_password", "Agenda2026")
        ui.onNodeWithTag("auth_submit").performScrollTo().performClick()
        ui.onNodeWithText("Hola, teclado").assertIsDisplayed()
        ui.onNodeWithText("Crear primera entrega").performScrollTo().performClick()
        type("delivery_title", "Entrega con teclado")
        type("delivery_notes", "Notas escritas con teclas")
        ui.onNodeWithTag("delivery_title").assertTextContains("Entrega con teclado")
    }
}
