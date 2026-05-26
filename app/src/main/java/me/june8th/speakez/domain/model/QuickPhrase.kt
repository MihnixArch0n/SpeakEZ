package me.june8th.speakez.domain.model

data class QuickPhrase(
    val id: String,
    val text: String,
    val actionType: ActionType,
    val actionPayload: String?,
)
