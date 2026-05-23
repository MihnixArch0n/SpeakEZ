package me.june8th.speakez.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import me.june8th.speakez.data.mulberry.MulberrySymbolRepository
import me.june8th.speakez.domain.model.MulberryCategory
import me.june8th.speakez.domain.model.MulberrySymbol
import me.june8th.speakez.tts.TtsManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    private val mulberrySymbolRepository: MulberrySymbolRepository,
) : ViewModel() {
    private val _sentenceWords = MutableStateFlow<List<String>>(emptyList())
    val sentenceWords: StateFlow<List<String>> = _sentenceWords.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _categories = MutableStateFlow<List<MulberryCategory>>(emptyList())
    val categories: StateFlow<List<MulberryCategory>> = _categories.asStateFlow()

    private val _symbols = MutableStateFlow<List<MulberrySymbol>>(emptyList())

    val filteredSymbols: StateFlow<List<MulberrySymbol>> = combine(
        _selectedCategory,
        _searchQuery,
        _symbols,
    ) { selectedCategory, searchQuery, symbols ->
        mulberrySymbolRepository.filterSymbols(
            symbols = symbols,
            query = searchQuery,
            categoryId = selectedCategory,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList(),
    )

    init {
        viewModelScope.launch {
            val symbols = mulberrySymbolRepository.getSymbols()
            _symbols.value = symbols
            _categories.value = mulberrySymbolRepository.getCategories(symbols)
            _isLoading.value = false
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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


