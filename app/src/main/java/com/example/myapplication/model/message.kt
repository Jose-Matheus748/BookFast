package com.example.bookfast.model

/**
 * Modelo de dados de uma mensagem no chat.
 *
 * @param text    Conteúdo da mensagem
 * @param isBot   true = mensagem do bot | false = mensagem do usuário
 * @param time    Hora de envio formatada (ex: "15:04")
 */
data class Message(
    val text: String,
    val isBot: Boolean,
    val time: String
)