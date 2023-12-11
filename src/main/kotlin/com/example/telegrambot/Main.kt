package com.example.telegrambot

import kotlinx.coroutines.runBlocking
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

data class Message(val chatId: Long, val text: String)

annotation class Command(val value: String)

// Controller base class
abstract class BaseController {
    suspend fun handleCommand(message: Message) {
        println(message)
    }
}

fun sendMessage(chatId: Long, text: String) {
    println("Sending message to $chatId: $text")
}

class TelegramBot(private val token: String) {
    private val controllers: List<BaseController> = findControllers()

    private fun findControllers(): List<BaseController> {
        val controllers = mutableListOf<BaseController>()

        try {
            val controllerClasses = findClassesWithAnnotation(Command::class)
            for (controllerClass in controllerClasses) {
                println("Found controller class: $controllerClass")
                val instance = controllerClass.createInstance()
                controllers.add(instance as BaseController)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return controllers
    }


    private fun findClassesWithAnnotation(annotation: KClass<out Annotation>): List<KClass<*>> {
        val controllers = mutableListOf<KClass<*>>()
        val packageName = "com.example.telegrambot.controllers"

        val reflections = Reflections(packageName)
        val annotated = reflections.getTypesAnnotatedWith(annotation.java)

        for (clazz in annotated) {
            controllers.add(clazz.kotlin)
        }

        return controllers
    }


    suspend fun handleLongPolling(message: Message) {
        // Handle long polling here
        // ...

        // Dispatch commands to controllers
        for (controller in controllers) {
            // Your logic to dispatch commands to controllers
            // For example, check the received command and call the appropriate method
            controller.handleCommand(message)
        }
    }
}

fun main(): Unit = runBlocking {
    val bot = TelegramBot("YOUR_TOKEN")
    val message = Message(123456, "/start")
    bot.handleLongPolling(message)
}
