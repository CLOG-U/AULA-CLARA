package ec.edu.aulaclara.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal sealed interface ScreenAction {
    data object Idle : ScreenAction
    data object Working : ScreenAction
    data class Saved(val id: Long) : ScreenAction
    data object Deleted : ScreenAction
    data class Message(val text: String) : ScreenAction
}

internal abstract class ActionViewModel : ViewModel() {
    private val pending = MutableStateFlow<ScreenAction>(ScreenAction.Idle)
    val action = pending.asStateFlow()

    protected fun perform(work: suspend () -> ScreenAction) {
        if (!pending.compareAndSet(ScreenAction.Idle, ScreenAction.Working)) return
        viewModelScope.launch {
            pending.value = try {
                work()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                ScreenAction.Message("No se pudo completar la operación. Inténtalo de nuevo.")
            }
        }
    }

    fun acknowledge(result: ScreenAction) {
        pending.compareAndSet(result, ScreenAction.Idle)
    }
}
