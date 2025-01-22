package org.wycliffeassociates.otter.common.domain.audio

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MarkerGenerator @Inject constructor() {

    fun generate(audioFile: File, verses: List<String>) {
        val markerPositions = parseMarker(audioFile, verses)
        if (markerPositions.size == verses.size) {
            val audio = OratureAudioFile(audioFile)
            markerPositions.forEachIndexed { index, pos ->
                val location = pos * DEFAULT_SAMPLE_RATE
                audio.addMarker(VerseMarker(index + 1, index + 1, location.toInt()))
            }
            audio.update()
        } else {
            println("---> this chapter has error!")
        }
    }

    private fun parseMarker(audioFile: File, verseList: List<String>): List<Double> {
        val transcription = transcribe(audioFile) ?: return listOf()
        val wordsWithMarker = findMarkerPositions(transcription.words, verseList)
        return wordsWithMarker.map { it.start }
    }

    private fun transcribe(inputFile: File): OpenAITranscription? {
        val apiKey = System.getenv("OPENAI_TTS_KEY")
        val client = OkHttpClient().newBuilder()
            .connectTimeout(10_000, TimeUnit.SECONDS)
            .readTimeout(10_000, TimeUnit.SECONDS)
            .build()

        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("timestamp_granularities[]", "word")
            .addFormDataPart("model", "whisper-1")
            .addFormDataPart("response_format", "verbose_json")
            .addFormDataPart(
                "file",
                "${inputFile.path}",
                inputFile.asRequestBody("application/octet-stream".toMediaType())
            )
            .build()
        val request = Request
            .Builder()
            .url("https://api.openai.com/v1/audio/transcriptions")
            .method("POST", body)
            .addHeader("Content-Type", "multipart/form-data")
            .addHeader(
                "Authorization",
                "Bearer $apiKey"
            )
            .build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Unexpected code $response")
                null
            }
            else {
                response.body?.string()?.let { data ->
                    val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
                    val transcription: OpenAITranscription = mapper.readValue(data)
                    transcription
                }
            }
        }
    }

    private fun findMarkerPositions(words: List<Word>, verses: List<String>): List<Word> {
        var markerCount = 0
        val chapterText = verses.map {
            "<${++markerCount}>$it"
        }.joinToString("\n")

        val wordListWithMarkers: List<String> = fillMarkers(chapterText, words.map { it.word })
        val markerPositions = mutableListOf<Int>()

        markerCount = 0
        wordListWithMarkers.forEachIndexed { index, w ->
           if (w.matches(Regex("<\\d{1,3}>"))) {
               markerCount++
               markerPositions.add(index + 1 - markerCount)
           }
        }

        return words.filterIndexed { index, _ ->
            index in markerPositions
        }
    }

    fun fillMarkers(originalText: String, words: List<String>): List<String> {
        val apiUrl = "https://api.openai.com/v1/chat/completions"
        val apiKey = System.getenv("OPENAI_KEY")

        val message = "I have a string containing <number> tags and a list of words in that string without punctuations. Your job is to create a new list of words including the tags that match the positions in the original string. The output must be a json array." +
                "Original string: \\\"$originalText\\\".\\n" +
                "Word list: $words"

        val objectMapper: ObjectMapper = ObjectMapper(JsonFactory()).registerKotlinModule()

        val jsonPayload = objectMapper.writeValueAsString(
            mapOf(
                "model" to "gpt-4o",
                "messages" to listOf(
                    mapOf("role" to "system", "content" to "You are a helpful assistant and you will generate responses in json format."),
                    mapOf("role" to "user", "content" to message)
                ),
                "temperature" to 0.1,
                "response_format" to mapOf("type" to "json_object")
            )
        )

        val client = HttpClient.newHttpClient()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            val data = response.body()
            val chatResponse = objectMapper.readValue<ChatResponse>(data)
            val answer = chatResponse.choices.first().message.content
            val responseObject: Map<String, List<String>> = objectMapper.readValue(answer)
            val results = responseObject["result"]!!
            return cleanUpPunctuations(results)
        } else {
            println("Request failed with status code: ${response.statusCode()}")
            println("Error response: ${response.body()}")
            throw Exception("ERROR while asking chatGPT!")
        }
    }

    private fun cleanUpPunctuations(words: List<String>): List<String> {
        val apiUrl = "https://api.openai.com/v1/chat/completions"
        val apiKey = System.getenv("OPENAI_KEY")

        val message = "Given a list of strings that contains words, some tags <number> and punctuations. " +
                "Please remove the punctuations from the list. The output must be a json array. The field in the output can be named \"results\". " +
                "List: $words"

        val objectMapper: ObjectMapper = ObjectMapper(JsonFactory()).registerKotlinModule()

        val jsonPayload = objectMapper.writeValueAsString(
            mapOf(
                "model" to "gpt-4o",
                "messages" to listOf(
                    mapOf("role" to "system", "content" to "You are a helpful assistant and you will generate responses in json format."),
                    mapOf("role" to "user", "content" to message)
                ),
                "temperature" to 0.1,
                "response_format" to mapOf("type" to "json_object")
            )
        )

        val client = HttpClient.newHttpClient()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            val data = response.body()
            val chatResponse = objectMapper.readValue<ChatResponse>(data)
            val answer = chatResponse.choices.first().message.content
            val responseObject: Map<String, List<String>> = objectMapper.readValue(answer)
            return responseObject["results"]!!
        } else {
            println("Request failed with status code: ${response.statusCode()}")
            println("Error response: ${response.body()}")
            throw Exception("ERROR while asking chatGPT!")
        }
    }

}

@JsonIgnoreProperties(ignoreUnknown=true)
data class OpenAITranscription(
    val task: String,
    val language: String,
    val duration: Double,
    val text: String,
    val words: List<Word>
)

data class Word(
    val word: String,
    val start: Double,
    val end: Double
)


@JsonIgnoreProperties(ignoreUnknown=true)
data class ChatResponse(
    val id: String,
    val model: String,
    val choices: List<Choice>,
)

@JsonIgnoreProperties(ignoreUnknown=true)
data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)

data class Message(
    val role: String,
    val content: String,
    val refusal: Any?
)