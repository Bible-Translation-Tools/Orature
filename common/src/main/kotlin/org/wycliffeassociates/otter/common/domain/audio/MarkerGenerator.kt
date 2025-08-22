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
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class MarkerGenerator @Inject constructor(
    private val directoryProvider: IDirectoryProvider
) {

    fun generate(audioFile: File, verses: List<String>) {
        val markerPositions = parseMarker(audioFile, verses)
        println(markerPositions.size)
        if (markerPositions.size != verses.size) {
            println("---> this chapter has error!")
        }

        val audio = OratureAudioFile(audioFile)
        audio.clearMarkers()
        markerPositions.forEachIndexed { index, pos ->
            val location = pos * DEFAULT_SAMPLE_RATE // convert secs to frames
            audio.addMarker(VerseMarker(index + 1, index + 1, location.toInt()))
        }
        audio.update()
    }

    private fun parseMarker(audioFile: File, verseList: List<String>): List<Double> {
        val transcription = transcribe(audioFile) ?: return listOf()
        val cleanVerseList = verseList.map {
            it.replace(",", " ")
                .replace(".", " ")
                .replace("\n"," ")
                .replace(Regex("  ")," ")
        }
        return findSubstringMatches(transcription.words, cleanVerseList)
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

        val wordsWithEasedTimestamp = words.mapIndexed { index, word ->
            if (index > 0) {
                val midPoint = (words[index - 1].end + word.start) / 2
                word.copy(start = midPoint) // offset to avoid cutting off the beginning of a word
            } else {
                word
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
                "temperature" to 0.0,
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
                "temperature" to 0.0,
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

    fun findSubstringMatches(transcription: List<Word>, substrings: List<String>): List<Double> {
        val apiUrl = "https://api.openai.com/v1/chat/completions"
        val apiKey = System.getenv("OPENAI_KEY")

        val transcriptionJson = transcription.map { 
            mapOf("word" to it.word, "start" to it.start) 
        }

        val message = """
            You are given a list of word-level transcription objects, each with a "word" and a "start" timestamp. 
            Your task is to find an approximate match for each query substring within the transcription. 
            Return the start timestamp of the first word in the matched sequence.
            
            Rules:
            - Matching should be case-insensitive and punctuation-insensitive.
            - Use fuzzy/soft matching: allow small variations like plural vs singular, minor rewordings, or slight differences in spelling.
            - Return -1 if no approximate match is found.
            
            Transcription: $transcriptionJson
            
            Substrings:
            ${substrings.mapIndexed { index, substring -> "${index + 1}. $substring" }.joinToString("\n")}
            
            Return a JSON object with a "matches" field containing an array of objects, each with "substring" and "start" fields.
            Example format: {"matches": [{"substring": "going to show", "start": 2.8}, {"substring": "bake a chocolate cakes", "start": 4.1}]}
        """.trimIndent()

        val objectMapper: ObjectMapper = ObjectMapper(JsonFactory()).registerKotlinModule()

        val jsonPayload = objectMapper.writeValueAsString(
            mapOf(
                "model" to "gpt-4o",
                "messages" to listOf(
                    mapOf("role" to "system", "content" to "You are a helpful assistant that analyzes transcriptions and finds approximate matches. Always return valid JSON with a 'matches' field containing an array of match objects with 'substring' and 'start' fields."),
                    mapOf("role" to "user", "content" to message)
                ),
                "temperature" to 0.0,
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
            val responseObject: Map<String, List<Map<String, Any>>> = objectMapper.readValue(answer)
            val matches = responseObject["matches"] ?: throw Exception("No 'matches' field in API response")
            
            return matches.map { match ->
                (match["start"] as Number).toDouble()
            }
        } else {
            println("Request failed with status code: ${response.statusCode()}")
            println("Error response: ${response.body()}")
            throw Exception("ERROR while finding substring matches with OpenAI API!")
        }
    }

    fun alignMarkersWithAeneas(audioFile: File, verses: List<String>): File {
        // Create temporary files
        val tempDir = directoryProvider.tempDirectory
        val versesFile = tempDir.resolve("verses.txt")
        val outputFile = tempDir.resolve("output.json")
        
        try {
            // Write verses to temporary file, each separated by line break
            versesFile.writeText(verses.joinToString("\n"))
            
            // Build the command
            val command = listOf(
                "python", "-m", "aeneas.tools.execute_task",
                audioFile.absolutePath,
                versesFile.absolutePath,
                "task_language=eng|os_task_file_format=json|is_text_type=plain",
                outputFile.absolutePath
            )
            
            // Execute the subprocess
            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                throw RuntimeException("Aeneas process failed with exit code $exitCode. Output: $output")
            }
            
            if (!outputFile.exists()) {
                throw RuntimeException("Output file was not created by aeneas process")
            }
            
            return outputFile
            
        } catch (e: Exception) {
            // Clean up temporary files on error
            try {
                versesFile.delete()
                outputFile.delete()
            } catch (cleanupException: Exception) {
                // Ignore cleanup errors
            }
            throw e
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

@JsonIgnoreProperties(ignoreUnknown=true)
data class Message(
    val role: String,
    val content: String,
    val refusal: Any?
)