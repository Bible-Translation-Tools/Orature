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
        } else {
            println("---> this chapter has error!")
        }
    }

    private fun parseMarker(audioFile: File, verseList: List<String>): List<Double> {
        val transcription = request(audioFile) ?: return listOf()
        println(transcription.words)
        val markersToAdd = mutableListOf<Double>()
        val wordArray = selfCorrect(transcription.words, verseList)

        var wordPosition = 0

        markersToAdd.add(0.0) // first marker
//        for (verse in verseList) {
//            while(verse.contains(wordArray[wordPosition].word)) {
//                wordPosition++
//            }
//            // position at the beginning of next verse
//            markerPositions.add(wordArray[wordPosition].start)
//        }
        for (verse in verseList) {
            var textOfVerse = verse

            while (wordPosition < wordArray.size) {
                val wordText = wordArray[wordPosition].word
                if (textOfVerse.contains(wordText)) {
                    textOfVerse = textOfVerse.substringAfter(wordText)
                    wordPosition++
                } else {
                    break
                }
            }
            // position at the beginning of next verse
            if (wordPosition < wordArray.size) {
                markersToAdd.add(wordArray[wordPosition].start)
            }
        }

        return markersToAdd
    }

    private fun request(inputFile: File): OpenAITranscription? {
        val apiKey = System.getenv("OPENAI_TTS_KEY")
        val client = OkHttpClient().newBuilder()
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

    private fun selfCorrect(words: List<Word>, verses: List<String>): List<Word> {
        val chapterText = verses.joinToString("\n")
        val correctedWords: Map<Int, String> = chatGPT(chapterText, words.map { it.word })
        val newWordList = words.toMutableList()

        correctedWords.keys.forEach { index ->
            val newText = correctedWords[index]!!
            val currentWord = newWordList[index]
            newWordList[index] = currentWord.copy(word = newText)
        }

        return newWordList
    }

    fun chatGPT(originalText: String, words: List<String>): Map<Int, String> {
        val apiUrl = "https://api.openai.com/v1/chat/completions"
        val apiKey = System.getenv("OPENAI_KEY")

        val message = "I have a string and a list of words parsed from that string.\\n" +
                "However, some words in the list may contain spelling errors.\\n" +
                "Generate a json object where the keys are the index of the incorrect word\\n" +
                "and the values are the corrected word that match the word from the original string. Example output: { \"1\":\"replacement\" }\\n" +
                "String: \\\"$originalText\\\"\\n" +
                "Words: $words"

        val objectMapper: ObjectMapper = ObjectMapper(JsonFactory()).registerKotlinModule()

        val jsonPayload = objectMapper.writeValueAsString(
            mapOf(
                "model" to "gpt-4o",
                "messages" to listOf(
                    mapOf("role" to "system", "content" to "You are a helpful assistant and you will generate responses in json format."),
                    mapOf("role" to "user", "content" to message)
                ),
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
            val correctionMap: Map<String, String> = objectMapper.readValue(answer)
            return correctionMap.mapKeys { it.key.toInt() }
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