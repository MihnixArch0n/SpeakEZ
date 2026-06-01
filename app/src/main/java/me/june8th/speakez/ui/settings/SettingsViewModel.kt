package me.june8th.speakez.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.june8th.speakez.data.mulberry.MulberrySymbolRepository
import me.june8th.speakez.data.settings.AppSettingsRepository
import me.june8th.speakez.data.settings.DEFAULT_FONT_SCALE
import me.june8th.speakez.data.settings.DEFAULT_PITCH
import me.june8th.speakez.data.settings.DEFAULT_SELECTED_VOICE_ID
import me.june8th.speakez.data.settings.DEFAULT_SPEECH_RATE
import me.june8th.speakez.data.word.CUSTOM_WORDS_CATEGORY_ID
import me.june8th.speakez.data.word.DuplicateCustomWordException
import me.june8th.speakez.data.word.WordAssetType
import me.june8th.speakez.data.word.WordEntity
import me.june8th.speakez.domain.model.MulberrySymbol
import me.june8th.speakez.domain.repository.WordRepository
import me.june8th.speakez.domain.validation.EmojiValidator
import me.june8th.speakez.tts.SystemVoiceOption
import me.june8th.speakez.tts.TtsManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val ttsManager: TtsManager,
    private val wordRepository: WordRepository,
    private val mulberrySymbolRepository: MulberrySymbolRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    init {
        val prefs = context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE)
        _speechRate.value = prefs.getFloat("speech_rate", 1.0f)
        _pitch.value = prefs.getFloat("pitch", 1.0f)
        ttsManager.setVoiceConfig(_speechRate.value, _pitch.value)
    }

    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _showLabels = MutableStateFlow(true)
    val showLabels: StateFlow<Boolean> = _showLabels.asStateFlow()
    val customWords: StateFlow<List<WordEntity>> = wordRepository.getAllCustomWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _customWordDraft = MutableStateFlow(CustomWordDraftState())
    val customWordDraft: StateFlow<CustomWordDraftState> = _customWordDraft.asStateFlow()

    private val _mulberrySymbols = MutableStateFlow<List<MulberrySymbol>>(emptyList())
    val mulberrySymbols: StateFlow<List<MulberrySymbol>> = _mulberrySymbols.asStateFlow()

    private val _customWordEvents = Channel<CustomWordEvent>(Channel.BUFFERED)
    val customWordEvents = _customWordEvents.receiveAsFlow()

    private val _draftFontScale = MutableStateFlow(DEFAULT_FONT_SCALE)
    private val _isFontScaleDirty = MutableStateFlow(false)

    private val effectiveFontScale = combine(
        appSettingsRepository.fontScale,
        _draftFontScale,
        _isFontScaleDirty,
    ) { persistedFontScale, draftFontScale, isDirty ->
        if (isDirty) draftFontScale else persistedFontScale
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        appSettingsRepository.settings,
        effectiveFontScale,
        ttsManager.vietnameseVoices,
        _volume,
        _showLabels,
    ) { settings, fontScale, voices, volume, showLabels ->
        SettingsUiState(
            speechRate = settings.speechRate,
            pitch = settings.pitch,
            fontScale = fontScale,
            selectedVoiceId = settings.selectedVoiceId,
            voiceOptions = voices,
            volume = volume,
            showLabels = showLabels,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    init {
        viewModelScope.launch {
            appSettingsRepository.fontScale.collect { persistedFontScale ->
                if (!_isFontScaleDirty.value) {
                    _draftFontScale.value = persistedFontScale
                }
            }
        }
        viewModelScope.launch {
            _mulberrySymbols.value = mulberrySymbolRepository.getSymbols()
        }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch {
            appSettingsRepository.setSpeechRate(rate)
        }
    }

    fun setPitch(pitch: Float) {
        viewModelScope.launch {
            appSettingsRepository.setPitch(pitch)
        }
    }

    fun setFontScale(fontScale: Float) {
        _draftFontScale.value = fontScale
        _isFontScaleDirty.value = true
    }

    fun setSelectedVoiceId(voiceId: String) {
        viewModelScope.launch {
            appSettingsRepository.setSelectedVoiceId(voiceId)
        }
    }

    fun setVolume(volume: Float) {
        _volume.value = volume
        // Volume is typically managed by system settings, but can be extended
    }

    fun setShowLabels(show: Boolean) {
        _showLabels.value = show
    }

    fun saveSettings() {
        viewModelScope.launch {
            if (_isFontScaleDirty.value) {
                appSettingsRepository.setFontScale(_draftFontScale.value)
                _isFontScaleDirty.value = false
            }
        }
        setVolume(_volume.value)
        setShowLabels(_showLabels.value)
        context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE)
            .edit()
            .putFloat("speech_rate", _speechRate.value)
            .putFloat("pitch", _pitch.value)
            .apply()
    }

    fun testAudio() {
        ttsManager.speak(
            text = "Xin chào, đây là giọng đọc thử nghiệm",
        )
    }

    fun updateCustomWordText(wordText: String) {
        _customWordDraft.value = _customWordDraft.value.copy(wordText = wordText, errorMessage = null)
    }

    fun selectCustomWordAssetType(assetType: WordAssetType) {
        _customWordDraft.value = _customWordDraft.value.copy(
            assetType = assetType,
            assetValue = "",
            errorMessage = null,
        )
    }

    fun selectCustomWordAsset(assetValue: String) {
        _customWordDraft.value = _customWordDraft.value.copy(assetValue = assetValue, errorMessage = null)
    }

    fun saveCustomWord() {
        val draft = _customWordDraft.value
        val error = validateCustomWordDraft(draft)
        if (error != null) {
            _customWordDraft.value = draft.copy(errorMessage = error)
            return
        }

        viewModelScope.launch {
            _customWordDraft.value = draft.copy(isSaving = true, errorMessage = null)
            wordRepository.addCustomWord(
                WordEntity(
                    wordText = draft.wordText.trim(),
                    categoryId = CUSTOM_WORDS_CATEGORY_ID,
                    assetType = draft.assetType,
                    assetValue = draft.assetValue,
                ),
            ).onSuccess {
                _customWordDraft.value = CustomWordDraftState()
                _customWordEvents.send(CustomWordEvent.Saved)
            }.onFailure { error ->
                _customWordDraft.value = draft.copy(
                    isSaving = false,
                    errorMessage = if (error is DuplicateCustomWordException) {
                        "Từ này đã tồn tại"
                    } else {
                        "Không thể lưu từ mới"
                    },
                )
            }
        }
    }

    fun deleteCustomWord(id: Long) {
        viewModelScope.launch {
            wordRepository.deleteWord(id)
        }
    }

    fun resetCustomWordDraft() {
        _customWordDraft.value = CustomWordDraftState()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }
}

data class CustomWordDraftState(
    val wordText: String = "",
    val assetType: WordAssetType = WordAssetType.MULBERRY,
    val assetValue: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface CustomWordEvent {
    data object Saved : CustomWordEvent
}

private fun validateCustomWordDraft(draft: CustomWordDraftState): String? {
    if (draft.wordText.isBlank()) return "Vui lòng nhập nội dung từ"
    if (draft.assetValue.isBlank()) return "Vui lòng chọn biểu tượng"
    if (draft.assetType == WordAssetType.EMOJI && !EmojiValidator.isSingleEmoji(draft.assetValue)) {
        return "Chỉ nhập một emoji"
    }
    return null
}

data class SettingsUiState(
    val speechRate: Float = DEFAULT_SPEECH_RATE,
    val pitch: Float = DEFAULT_PITCH,
    val fontScale: Float = DEFAULT_FONT_SCALE,
    val selectedVoiceId: String = DEFAULT_SELECTED_VOICE_ID,
    val voiceOptions: List<SystemVoiceOption> = emptyList(),
    val volume: Float = 0.8f,
    val showLabels: Boolean = true,
)
