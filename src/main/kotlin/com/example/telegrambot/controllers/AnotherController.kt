package com.example.telegrambot.controllers

import com.example.telegrambot.BaseController
import com.example.telegrambot.Command
import com.example.telegrambot.Message
import com.example.telegrambot.sendMessage

class AnotherController : BaseController() {
    @Command("/another")
     suspend fun another(message: Message) {
        sendMessage(message.chat.id, "This is another command.")
    }
}