package com.example.telegrambot.controllers

import com.example.telegrambot.Command
import com.example.telegrambot.Message
import com.example.telegrambot.sendMessage

class StartController {
    @Command("/start")
     suspend fun start(message: Message) {
        println("Hello, Kotlin!")
        sendMessage(message.chat.id, "Hello, what's your name?")
     }

    @Command("/help")
    suspend fun help(message: Message) {
        sendMessage(message.chat.id, "Hello, what's your name?")
    }
}