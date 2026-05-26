package me.june8th.speakez.domain.usecase.quickphrase

import me.june8th.speakez.domain.model.QuickPhrase
import me.june8th.speakez.domain.repository.QuickPhraseRepository
import javax.inject.Inject

class AddQuickPhraseUseCase @Inject constructor(
    private val repository: QuickPhraseRepository,
) {
    suspend operator fun invoke(quickPhrase: QuickPhrase) {
        repository.addQuickPhrase(quickPhrase)
    }
}
