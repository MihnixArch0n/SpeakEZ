package me.june8th.speakez.domain.repository

import kotlinx.coroutines.flow.Flow
import me.june8th.speakez.domain.model.QuickPhrase

interface QuickPhraseRepository {
    fun getQuickPhrases(): Flow<List<QuickPhrase>>

    suspend fun addQuickPhrase(quickPhrase: QuickPhrase)

    suspend fun updateQuickPhrase(quickPhrase: QuickPhrase)

    suspend fun deleteQuickPhrase(id: String)
}
