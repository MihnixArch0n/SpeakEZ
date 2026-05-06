package me.june8th.speakez.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.june8th.speakez.data.mock.MockVocabularyRepository
import me.june8th.speakez.domain.model.VocabularyItem
import me.june8th.speakez.tts.TtsManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ttsManager: TtsManager,
) : ViewModel() {
    private val _sentenceWords = MutableStateFlow<List<String>>(emptyList())
    val sentenceWords: StateFlow<List<String>> = _sentenceWords.asStateFlow()

    private val _vocabulary = MutableStateFlow<List<VocabularyItem>>(emptyList())
    val vocabulary: StateFlow<List<VocabularyItem>> = _vocabulary.asStateFlow()

    init {
        loadMockVocabulary()
    }

    private fun loadMockVocabulary() {
        _vocabulary.value = MockVocabularyRepository.getCategories()
    }

    fun addWord(word: String) {
        _sentenceWords.value = _sentenceWords.value + word
        ttsManager.speak(word)
    }

    fun removeLastWord() {
        _sentenceWords.value = if (_sentenceWords.value.isNotEmpty()) {
            _sentenceWords.value.dropLast(1)
        } else {
            emptyList()
        }
    }

    fun clearSentence() {
        _sentenceWords.value = emptyList()
    }

    fun getSentence(): String = _sentenceWords.value.joinToString(" ")

    fun speakSentence() {
        val sentence = getSentence()
        if (sentence.isNotBlank()) {
            ttsManager.speak(sentence)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }
}



