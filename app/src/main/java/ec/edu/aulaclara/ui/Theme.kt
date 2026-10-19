package ec.edu.aulaclara.ui

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF007F78), onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF3EE), onPrimaryContainer = Color(0xFF005A55),
    secondary = Color(0xFF526D87), secondaryContainer = Color(0xFFE6EDF7),
    background = Color(0xFFF3F8FA), onBackground = Color(0xFF183447),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF183447),
    surfaceVariant = Color(0xFFEAF0F3), onSurfaceVariant = Color(0xFF536775),
    outline = Color(0xFF758A94), outlineVariant = Color(0xFFD7E3E8),
    error = Color(0xFFB23D52), errorContainer = Color(0xFFFFE9ED),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF78D9CC), onPrimary = Color(0xFF003D38),
    primaryContainer = Color(0xFF164D48), onPrimaryContainer = Color(0xFFAAEEE2),
    secondary = Color(0xFFADC6E1), secondaryContainer = Color(0xFF304458),
    background = Color(0xFF101F29), onBackground = Color(0xFFDFEAF0),
    surface = Color(0xFF192C38), onSurface = Color(0xFFDFEAF0),
    surfaceVariant = Color(0xFF263C47), onSurfaceVariant = Color(0xFFB3C6CE),
    outline = Color(0xFF8EA7B1), outlineVariant = Color(0xFF3B535E),
    error = Color(0xFFFFA9B8), errorContainer = Color(0xFF572332),
)

@Composable
fun AulaClaraTheme(theme: String = "Sistema", content: @Composable () -> Unit) {
    val dark = theme == "Oscuro" || (theme == "Sistema" && isSystemInDarkTheme())
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val activity = generateSequence(view.context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>().firstOrNull()
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    val typography = Typography(
        headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 39.sp),
        headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 34.sp),
        titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 29.sp),
        titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
        bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
        labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp),
    )
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = typography,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(24.dp)),
        content = content,
    )
}
