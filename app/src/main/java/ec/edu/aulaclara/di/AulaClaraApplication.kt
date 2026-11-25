package ec.edu.aulaclara.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ec.edu.aulaclara.data.AgendaRepository
import ec.edu.aulaclara.data.FakeAgendaRepository

class AulaClaraApplication : Application() {
    private val repository: AgendaRepository by lazy { FakeAgendaRepository() }

    internal inline fun <reified T : ViewModel> factory(
        crossinline create: (AgendaRepository) -> T,
    ): ViewModelProvider.Factory = viewModelFactory {
        initializer { create(agendaRepository) }
    }

    @PublishedApi
    internal val agendaRepository: AgendaRepository get() = repository
}
