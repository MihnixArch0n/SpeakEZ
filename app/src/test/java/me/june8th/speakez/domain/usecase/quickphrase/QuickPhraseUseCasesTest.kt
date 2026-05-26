package me.june8th.speakez.domain.usecase.quickphrase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.model.QuickPhrase
import me.june8th.speakez.domain.repository.QuickPhraseRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickPhraseUseCasesTest {
    private val initialPhrase = QuickPhrase(
        id = "phrase-1",
        text = "Con cần giúp đỡ",
        actionType = ActionType.PUSH_NOTI,
        actionPayload = "helper",
    )

    @Test
    fun getQuickPhrases_returnsRepositoryFlow() = runBlocking {
        val repository = FakeQuickPhraseRepository(listOf(initialPhrase))
        val useCase = GetQuickPhrasesUseCase(repository)

        assertEquals(listOf(initialPhrase), useCase().first())
    }

    @Test
    fun addQuickPhrase_delegatesToRepository() = runBlocking {
        val repository = FakeQuickPhraseRepository()
        val useCase = AddQuickPhraseUseCase(repository)

        useCase(initialPhrase)

        assertEquals(initialPhrase, repository.addedPhrase)
        assertEquals(listOf(initialPhrase), repository.getQuickPhrases().first())
    }

    @Test
    fun updateQuickPhrase_delegatesToRepository() = runBlocking {
        val repository = FakeQuickPhraseRepository(listOf(initialPhrase))
        val useCase = UpdateQuickPhraseUseCase(repository)
        val updatedPhrase = initialPhrase.copy(
            text = "Gọi người thân",
            actionType = ActionType.CALL,
            actionPayload = "0900000000",
        )

        useCase(updatedPhrase)

        assertEquals(updatedPhrase, repository.updatedPhrase)
        assertEquals(listOf(updatedPhrase), repository.getQuickPhrases().first())
    }

    @Test
    fun deleteQuickPhrase_delegatesToRepository() = runBlocking {
        val repository = FakeQuickPhraseRepository(listOf(initialPhrase))
        val useCase = DeleteQuickPhraseUseCase(repository)

        useCase(initialPhrase.id)

        assertEquals(initialPhrase.id, repository.deletedId)
        assertEquals(emptyList<QuickPhrase>(), repository.getQuickPhrases().first())
    }

    @Test
    fun executeEmergencyAction_acceptsAllActionTypes() {
        val useCase = ExecuteEmergencyActionUseCase()

        ActionType.entries.forEach { actionType ->
            useCase(actionType, "payload")
            useCase(actionType, null)
        }
    }

    @Test
    fun fakeRepository_startsWithoutDelegationCalls() {
        val repository = FakeQuickPhraseRepository()

        assertNull(repository.addedPhrase)
        assertNull(repository.updatedPhrase)
        assertNull(repository.deletedId)
    }
}

private class FakeQuickPhraseRepository(
    initialPhrases: List<QuickPhrase> = emptyList(),
) : QuickPhraseRepository {
    private val phrases = MutableStateFlow(initialPhrases)

    var addedPhrase: QuickPhrase? = null
        private set

    var updatedPhrase: QuickPhrase? = null
        private set

    var deletedId: String? = null
        private set

    override fun getQuickPhrases(): Flow<List<QuickPhrase>> = phrases

    override suspend fun addQuickPhrase(quickPhrase: QuickPhrase) {
        addedPhrase = quickPhrase
        phrases.value = phrases.value + quickPhrase
    }

    override suspend fun updateQuickPhrase(quickPhrase: QuickPhrase) {
        updatedPhrase = quickPhrase
        phrases.value = phrases.value.map { existing ->
            if (existing.id == quickPhrase.id) quickPhrase else existing
        }
    }

    override suspend fun deleteQuickPhrase(id: String) {
        deletedId = id
        phrases.value = phrases.value.filterNot { quickPhrase -> quickPhrase.id == id }
    }
}
