package ec.edu.aulaclara.data

import java.time.LocalDateTime
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAgendaRepositoryTest {
    @Test fun concurrentConsumersKeepDeliveryAndCatalogConsistent() = runBlocking {
        val repository = FakeAgendaRepository()
        repository.signIn("Ana", "ana@ejemplo.com", false)
        assertTrue(repository.saveCatalogItem(null, false, "Programación", 0))
        val subject = repository.agenda.value.subjects.single()
        val draft = Delivery(0, "Proyecto", subject.id, 1, LocalDateTime.of(2026, 11, 25, 18, 0))
        val first = async { repository.saveDelivery(draft) }
        val second = async { repository.saveDelivery(draft.copy(title = "Lectura")) }
        val ids = listOfNotNull(first.await(), second.await())
        assertEquals(2, ids.toSet().size)
        assertFalse(repository.deleteCatalogItem(subject.id, false))
        assertTrue(repository.setCompleted(ids.first(), true))
        assertNotNull(repository.saveDelivery(draft.copy(id = ids.first(), title = "Proyecto final")))
        assertTrue(repository.agenda.value.deliveries.first { it.id == ids.first() }.completed)
        repository.signOut()
        assertEquals(Agenda(), repository.agenda.value)
        assertNull(repository.saveDelivery(draft))
    }
}
