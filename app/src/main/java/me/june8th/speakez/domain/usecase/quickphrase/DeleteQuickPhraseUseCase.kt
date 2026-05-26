package me.june8th.speakez.domain.usecase.quickphrase

import me.june8th.speakez.domain.repository.QuickPhraseRepository
import javax.inject.Inject

class DeleteQuickPhraseUseCase @Inject constructor(
    private val repository: QuickPhraseRepository,
) {
    suspend operator fun invoke(id: String) {
        repository.deleteQuickPhrase(id)
    }
}
