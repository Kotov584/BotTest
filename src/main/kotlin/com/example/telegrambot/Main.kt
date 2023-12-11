package com.example.telegrambot

import kotlinx.coroutines.runBlocking
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.declaredMemberFunctions
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class Message(
    val message_id: Int,
    val from: From,
    val chat: Chat,
    val date: Int,
    val text: String
)

@Serializable
data class From(
    val id: Int,
    val is_bot: Boolean,
    val first_name: String,
    val username: String,
    val language_code: String
)

@Serializable
data class Chat(
    val id: Int,
    val first_name: String,
    val username: String,
    val type: String
)

@Serializable
data class CallBackQuery(
    val id: String,
    val from: From,
    val message: Message,
    val chat_instance: String,
    val data: String
)

@Serializable
data class Update(
    val update_id: Int,
    val message: Message? = null,
    val callback_query: CallBackQuery? = null
)

@Serializable
data class GetUpdatesResponse(
    val ok: Boolean, val result: List<Update>
)

annotation class Command(val value: String)

// Controller base class
abstract class BaseController {
    suspend fun handleCommand(message: Message) {
        println(message)
    }
}

fun sendMessage(chatId: Int, text: String) {
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

    suspend fun handleLongPolling() {
        var offset = 0
        while (true) {
            val updates = getUpdates(offset)
            for (update in updates) {
                handleUpdate(update)
                offset = update.update_id + 1
            }
        }
    }

    suspend fun getUpdates(offset: Int): List<Update> = withContext(Dispatchers.IO) {
        val url = URL("https://api.telegram.org/bot$token/getUpdates?offset=$offset")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val updatesResponse = Json { ignoreUnknownKeys = true }.decodeFromString<GetUpdatesResponse>(response)

        if (!updatesResponse.ok) {
            throw Exception("Failed to get updates from Telegram Bot API")
        }

        updatesResponse.result // return is removed
    }

    suspend fun handleUpdate(update: Update) {
        println(update)
        // Process the update.
    }
}

fun main(): Unit = runBlocking {
    val bot = TelegramBot("5367105785:AAES4Om_H-twvhK3RbKDT_YXlF985us2CpA")
    bot.handleLongPolling()
}