package ec.edu.aulaclara.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Small, local set of stroke vectors; no full icon catalog is shipped in the APK. */
object AulaIcons {
    private fun vector(name: String, path: String, mirrored: Boolean = false): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = mirrored).apply {
            addPath(PathParser().parsePathString(path).toNodes(), pathFillType = PathFillType.NonZero,
                fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }.build()

    val Add = vector("Add", "M12,5 L12,19 M5,12 L19,12")
    val Check = vector("Check", "M5,12 L10,17 L20,6")
    val Close = vector("Close", "M6,6 L18,18 M18,6 L6,18")
    val ChevronRight = vector("ChevronRight", "M9,5 L16,12 L9,19", true)
    val ArrowBack = vector("ArrowBack", "M19,12 L5,12 M12,5 L5,12 L12,19", true)
    val Home = vector("Home", "M3,10 L12,3 L21,10 M5,9 L5,21 L10,21 L10,15 L14,15 L14,21 L19,21 L19,9")
    val MenuBook = vector("MenuBook", "M12,5 C9,3 5,3 2,4 L2,19 C5,18 9,18 12,20 C15,18 19,18 22,19 L22,4 C19,3 15,3 12,5 L12,20 M5,7 L9,8 M15,8 L19,7", true)
    val Settings = vector("Settings", "M9,3 L15,3 L16,6 L19,7 L22,11 L20,14 L20,18 L16,20 L13,19 L9,21 L5,19 L5,15 L2,12 L4,8 L8,7 Z M16,12 A4,4 0,1 1,8 12 A4,4 0,1 1,16 12")
    val TaskAlt = vector("TaskAlt", "M21,11 A9,9 0,1 1,12 3 M8,11 L12,15 L22,4")
    val Info = vector("Info", "M21,12 A9,9 0,1 1,3 12 A9,9 0,1 1,21 12 M12,11 L12,17 M12,7 L12,7.2")
    val DeleteOutline = vector("DeleteOutline", "M4,6 L20,6 M9,6 L9,3 L15,3 L15,6 M6,6 L7,21 L17,21 L18,6 M10,10 L10,17 M14,10 L14,17")
    val EditNote = vector("EditNote", "M4,5 L16,5 M4,10 L13,10 M4,15 L10,15 M13,19 L14,15 L20,9 L23,12 L17,18 Z")
    val Event = vector("Event", "M5,4 L19,4 Q21,4 21,6 L21,20 L3,20 L3,6 Q3,4 5,4 M3,9 L21,9 M7,2 L7,6 M17,2 L17,6 M7,13 L10,13 L10,16 L7,16 Z")
    val CalendarMonth = Event
    val Schedule = vector("Schedule", "M21,12 A9,9 0,1 1,3 12 A9,9 0,1 1,21 12 M12,6 L12,12 L16,14")
    val Search = vector("Search", "M17,10 A7,7 0,1 1,3 10 A7,7 0,1 1,17 10 M15,15 L21,21")
    val SearchOff = vector("SearchOff", "M17,10 A7,7 0,1 1,3 10 A7,7 0,1 1,17 10 M15,15 L21,21 M7,7 L13,13 M13,7 L7,13")
    val Tune = vector("Tune", "M3,6 L8,6 M12,6 L21,6 M3,12 L14,12 M18,12 L21,12 M3,18 L6,18 M10,18 L21,18 M8,3 L12,3 L12,9 L8,9 Z M14,9 L18,9 L18,15 L14,15 Z M6,15 L10,15 L10,21 L6,21 Z")
    val Visibility = vector("Visibility", "M2,12 Q12,-2 22,12 Q12,26 2,12 Z M15,12 A3,3 0,1 1,9 12 A3,3 0,1 1,15 12")
    val VisibilityOff = vector("VisibilityOff", "M2,12 Q12,-2 22,12 Q12,26 2,12 Z M3,3 L21,21")
    val Circle = vector("Circle", "M21,12 A9,9 0,1 1,3 12 A9,9 0,1 1,21 12")
    val Link = vector("Link", "M9,8 L7,8 A4,4 0,0 0,7 16 L10,16 M15,8 L17,8 A4,4 0,0 1,17 16 L14,16 M8,12 L16,12")
    val NotificationsNone = vector("NotificationsNone", "M5,16 L7,13 L7,8 A5,5 0,0 1,17 8 L17,13 L19,16 L5,16 M10,20 Q12,23 14,20 M12,2 L12,3")
    val Logout = vector("Logout", "M10,3 L3,3 L3,21 L10,21 M9,12 L22,12 M17,7 L22,12 L17,17", true)
}
