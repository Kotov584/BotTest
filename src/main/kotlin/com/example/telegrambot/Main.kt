package com.example.telegrambot

import kotlinx.coroutines.runBlocking
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.declaredMemberFunctions

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
        val reflections = Reflections("com.example.telegrambot.controllers")
        val controllerClasses = reflections.getSubTypesOf(BaseController::class.java)

        for (controllerClass in controllerClasses) {
            val instance = controllerClass.kotlin.createInstance()
            controllers.add(instance as BaseController)
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
        val text = message.text

        for (controller in controllers) {
            for (method in controller::class.declaredMemberFunctions) {
                val annotationValue = method.findAnnotation<Command>()
                if (annotationValue?.value == text) {
                    val kFunction = method as? KFunction<*>
                    kFunction?.call(controller, 123123, message)
                    //(method as KFunction<SuspendFunction1<*, *>>).callSuspend(controller, message)
                    println(controller)
                    println(method)
                    println(annotationValue.value)
                    return
                }
            }
        }

        println("No handler found for command: $text")
    }
}

fun main(): Unit = runBlocking {
    val bot = TelegramBot("YOUR_TOKEN")
    val message = Message(123456, "/start")
    bot.handleLongPolling(message)
}