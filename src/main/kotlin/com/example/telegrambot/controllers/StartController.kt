package com.example.telegrambot.controllers

import com.example.telegrambot.BaseController
import com.example.telegrambot.Command
import com.example.telegrambot.Message
import com.example.telegrambot.sendMessage

class StartController : BaseController() {
    @Command("/start")
     suspend fun start(message: Message) {
        sendMessage(message.chatId, "Hello, what's your name?")
    }

    @Command("/help")
    suspend fun help(message: Message) {
        sendMessage(message.chatId, "Hello, what's your name?")
    }
}