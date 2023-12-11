package com.example.telegrambot

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URISyntaxException
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
    val ok: Boolean,
    val result: List<Update>
)

annotation class Command(val value: String)

fun sendMessage(chatId: Int, text: String) {
    println("Sending message to $chatId: $text")
}

class TelegramBot(private val token: String) {
    private val controllers: List<Any> = findControllers()

    private fun findControllers(): List<Any> {
        val controllerClasses = mutableListOf<Any>()
        val packageName = "com.example.telegrambot.controllers"

        try {
            val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
            val packagePath = packageName.replace(".", "/")
            val resources = classLoader.getResources(packagePath)

            while (resources.hasMoreElements()) {
                val url = resources.nextElement()
                val file = File(url.toURI())
                if (file.isDirectory) {
                    val classNames = file.listFiles { _, name -> name.endsWith(".class") }
                        ?.map { it.nameWithoutExtension }
                        ?: emptyList()

                    for (className in classNames) {
                        val fullClassName = "$packageName.$className"
                        try {
                            val clazz = Class.forName(fullClassName)
                            if (Any::class.java.isAssignableFrom(clazz)) {
                                val controller = clazz.getDeclaredConstructor().newInstance()
                                controllerClasses.add(controller)
                            }
                        } catch (e: ClassNotFoundException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        return controllerClasses
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
        println(controllers)
        println(update)
        // Process the update.
    }
}

fun main(): Unit = runBlocking {
    val bot = TelegramBot("5367105785:AAES4Om_H-twvhK3RbKDT_YXlF985us2CpA")
    bot.handleLongPolling()
}