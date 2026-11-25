package ec.edu.aulaclara.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import ec.edu.aulaclara.data.AgendaRepository
import ec.edu.aulaclara.di.AulaClaraApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class Destination(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AulaClaraApp() {
    val model = agendaViewModel(::AppViewModel)
    val state by model.uiState.collectAsStateWithLifecycle()
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
        fun replaceStack(destination: String) {
            nav.navigate(destination) { popUpTo(nav.graph.id) { inclusive = true } }
        }
        LaunchedEffect(route, state.signedIn) {
            val current = route ?: return@LaunchedEffect
            val auth = current == "login" || current == "register"
            if (current == "splash") {
                delay(450)
                replaceStack(if (state.signedIn) "home" else "login")
            } else if (auth && state.signedIn) {
                replaceStack("home")
            } else if (!auth && !state.signedIn) {
                roots.forEach { nav.clearBackStack(it.route) }
                replaceStack("login")
            }
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
                composable("splash") { SplashScreen() }
                composable("login") { AuthRoute(false) { nav.navigate("register") } }
                composable("register") { AuthRoute(true, back) }
                composable("home") {
                    val vm = agendaViewModel(::HomeViewModel)
                    val screenState by vm.uiState.collectAsStateWithLifecycle()
                    HomeScreen(
                        screenState, newDelivery,
                        onAll = { nav.navigate("deliveries") },
                        onDetail = { nav.navigate("delivery/$it") },
                    )
                }
                composable("deliveries") {
                    val vm = agendaViewModel(::DeliveryListViewModel)
                    val screenState by vm.uiState.collectAsStateWithLifecycle()
                    DeliveryListScreen(
                        screenState, back, newDelivery, { nav.navigate("delivery/$it") },
                        vm::setQuery, vm::applyFilters, vm::clearFilters,
                    )
                }
                val newSubject = { nav.navigate("subject/new") }
                val newType = { nav.navigate("type/new") }
                composable("delivery/new") {
                    DeliveryFormRoute(null, back, newSubject, newType, notify) { id ->
                        nav.popBackStack()
                        nav.navigate("delivery/$id")
                        notify("Entrega guardada")
                    }
                }
                composable("delivery/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                    val id = e.arguments!!.getLong("id")
                    val vm: DeliveryDetailViewModel = agendaViewModel { DeliveryDetailViewModel(it, id) }
                    val screenState by vm.uiState.collectAsStateWithLifecycle()
                    val action by vm.action.collectAsStateWithLifecycle()
                    DeliveryDetailScreen(
                        screenState, action == ScreenAction.Working, back, { nav.navigate("delivery/edit/$id") },
                        vm::toggleCompleted, vm::delete,
                    )
                    HandleAction(action, vm::acknowledge) { result ->
                        when (result) {
                            ScreenAction.Deleted -> { back(); notify("Entrega eliminada") }
                            is ScreenAction.Message -> notify(result.text)
                            else -> Unit
                        }
                    }
                }
                composable("delivery/edit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                    DeliveryFormRoute(e.arguments!!.getLong("id"), back, newSubject, newType, notify) {
                        nav.popBackStack()
                        notify("Cambios guardados")
                    }
                }
                composable("catalog") {
                    val vm = agendaViewModel(::CatalogViewModel)
                    val screenState by vm.uiState.collectAsStateWithLifecycle()
                    CatalogScreen(screenState, vm::selectTypes, { nav.navigate(subjectRoute(it)) }, { nav.navigate(typeRoute(it)) })
                }
                composable("subject/new") { CatalogEditorRoute(null, false, back, notify) }
                composable("subject/edit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                    CatalogEditorRoute(e.arguments!!.getLong("id"), false, back, notify)
                }
                composable("type/new") { CatalogEditorRoute(null, true, back, notify) }
                composable("type/edit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                    CatalogEditorRoute(e.arguments!!.getLong("id"), true, back, notify)
                }
                composable("settings") {
                    val vm = agendaViewModel(::SettingsViewModel)
                    val screenState by vm.uiState.collectAsStateWithLifecycle()
                    SettingsScreen(screenState, vm::setTheme, vm::setReminders, vm::setOrder, vm::signOut)
                }
            }
            // Reserve measured space: messages must not intercept the bottom actions
            // of a scrolled form or detail screen on compact phones.
            SnackbarHost(snackbars, Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private inline fun <reified T : ViewModel> agendaViewModel(crossinline create: (AgendaRepository) -> T): T {
    val app = LocalContext.current.applicationContext as AulaClaraApplication
    return viewModel(factory = app.factory<T> { create(it) })
}

@Composable
private fun AuthRoute(register: Boolean, onSwitch: () -> Unit) {
    val vm: AuthViewModel = agendaViewModel { AuthViewModel(it, register) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    AuthScreen(register, state, onSwitch, vm::setName, vm::setEmail, vm::setPassword, vm::setConfirmation, vm::submit, vm::exploreDemo)
}

@Composable
private fun DeliveryFormRoute(
    id: Long?,
    onBack: () -> Unit,
    onNewSubject: () -> Unit,
    onNewType: () -> Unit,
    notify: (String) -> Unit,
    onSaved: (Long) -> Unit,
) {
    val vm: DeliveryFormViewModel = agendaViewModel { DeliveryFormViewModel(it, id) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val action by vm.action.collectAsStateWithLifecycle()
    DeliveryFormScreen(state, action == ScreenAction.Working, onBack, onNewSubject, onNewType, vm::updateDraft, vm::save)
    HandleAction(action, vm::acknowledge) { result ->
        when (result) {
            is ScreenAction.Saved -> onSaved(result.id)
            is ScreenAction.Message -> notify(result.text)
            else -> Unit
        }
    }
}

@Composable
private fun CatalogEditorRoute(id: Long?, isType: Boolean, onBack: () -> Unit, notify: (String) -> Unit) {
    val vm: CatalogEditorViewModel = agendaViewModel { CatalogEditorViewModel(it, id, isType) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val action by vm.action.collectAsStateWithLifecycle()
    CatalogEditorScreen(state, action == ScreenAction.Working, onBack, vm::setName, vm::setColor, vm::save, vm::delete)
    HandleAction(action, vm::acknowledge) { result ->
        val noun = if (isType) "Tipo" else "Materia"
        when (result) {
            is ScreenAction.Saved -> {
                onBack()
                notify(if (id != null) "Cambios guardados" else if (isType) "Tipo creado" else "Materia creada")
            }
            ScreenAction.Deleted -> {
                onBack()
                notify(if (isType) "$noun eliminado" else "$noun eliminada")
            }
            is ScreenAction.Message -> notify(result.text)
            else -> Unit
        }
    }
}

@Composable
private fun HandleAction(action: ScreenAction, acknowledge: (ScreenAction) -> Unit, onResult: (ScreenAction) -> Unit) {
    LaunchedEffect(action) {
        if (action != ScreenAction.Idle && action != ScreenAction.Working) {
            acknowledge(action)
            onResult(action)
        }
    }
}
