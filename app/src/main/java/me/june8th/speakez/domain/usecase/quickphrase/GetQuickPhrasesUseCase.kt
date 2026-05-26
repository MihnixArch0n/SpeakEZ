package me.june8th.speakez.domain.usecase.quickphrase

import kotlinx.coroutines.flow.Flow
import me.june8th.speakez.domain.model.QuickPhrase
import me.june8th.speakez.domain.repository.QuickPhraseRepository
import javax.inject.Inject

class GetQuickPhrasesUseCase @Inject constructor(
    private val repository: QuickPhraseRepository,
) {
    operator fun invoke(): Flow<List<QuickPhrase>> = repository.getQuickPhrases()
}
