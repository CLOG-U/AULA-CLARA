package ec.edu.aulaclara.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class Destination(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AulaClaraApp() {
    val state = rememberSaveable(saver = AgendaState.Saver) { AgendaState() }
    AulaClaraTheme(state.theme) {
        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        val route = entry?.destination?.route
        val snackbars = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val notify: (String) -> Unit = { message -> scope.launch {
            snackbars.currentSnackbarData?.dismiss()
            snackbars.showSnackbar(message)
        }; Unit }
        val roots = listOf(Destination("home", "Inicio", AulaIcons.Home), Destination("catalog", "Materias", AulaIcons.MenuBook), Destination("settings", "Ajustes", AulaIcons.Settings))
        val back: () -> Unit = { nav.popBackStack(); Unit }
        val newDelivery: () -> Unit = { nav.navigate("delivery/new") }
        fun subjectRoute(id: Long?) = if (id == null) "subject/new" else "subject/edit/$id"
        fun typeRoute(id: Long?) = if (id == null) "type/new" else "type/edit/$id"
        val enter: (String, String, Boolean) -> Unit = { name, email, demo ->
            state.startSession(name, email, demo)
            nav.navigate("home") { popUpTo(nav.graph.id) { inclusive = true } }
        }
        Scaffold(containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (state.signedIn && roots.any { it.route == route }) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    roots.forEach { destination -> NavigationBarItem(selected = route == destination.route,
                        onClick = {
                            nav.navigate(destination.route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        }, icon = { Icon(destination.icon, null) }, label = { Text(destination.label) }, modifier = Modifier.testTag("nav_${destination.route}")) }
                }
            }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            NavHost(nav, startDestination = "splash", modifier = Modifier.weight(1f).fillMaxWidth()) {
                composable("splash") {
                    SplashScreen()
                    LaunchedEffect(Unit) { delay(450); nav.navigate(if (state.signedIn) "home" else "login") { popUpTo("splash") { inclusive = true } } }
                }
                composable("login") { AuthScreen(false, { nav.navigate("register") }, enter) }
                composable("register") { AuthScreen(true, back, enter) }
                composable("home") { HomeScreen(state, newDelivery, { nav.navigate("deliveries") }, { nav.navigate("delivery/$it") }) }
                composable("deliveries") { DeliveryListScreen(state, back, newDelivery, { nav.navigate("delivery/$it") }) }
                composable("delivery/new") { DeliveryFormScreen(state, null, back, { id ->
                    nav.popBackStack(); nav.navigate("delivery/$id"); notify("Entrega guardada")
                }, { nav.navigate("subject/new") }, { nav.navigate("type/new") }) }
                composable("delivery/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                    val id = e.arguments!!.getLong("id")
                    DeliveryDetailScreen(state, id, back, { nav.navigate("delivery/edit/$id") }, back, notify)
                }
                composable("delivery/edit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                    DeliveryFormScreen(state, e.arguments!!.getLong("id"), back, { nav.popBackStack(); notify("Cambios guardados") },
                        { nav.navigate("subject/new") }, { nav.navigate("type/new") })
                }
                composable("catalog") { CatalogScreen(state, { nav.navigate(subjectRoute(it)) }, { nav.navigate(typeRoute(it)) }) }
                composable("subject/new") { CatalogEditorScreen(state, null, false, back, notify) }
                composable("subject/edit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e -> CatalogEditorScreen(state, e.arguments!!.getLong("id"), false, back, notify) }
                composable("type/new") { CatalogEditorScreen(state, null, true, back, notify) }
                composable("type/edit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e -> CatalogEditorScreen(state, e.arguments!!.getLong("id"), true, back, notify) }
                composable("settings") { SettingsScreen(state) { state.signOut(); nav.navigate("login") { popUpTo(nav.graph.id) { inclusive = true } } } }
            }
            // Reserve measured space: messages must not intercept the bottom actions
            // of a scrolled form or detail screen on compact phones.
            SnackbarHost(snackbars, Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
        }
    }
}
