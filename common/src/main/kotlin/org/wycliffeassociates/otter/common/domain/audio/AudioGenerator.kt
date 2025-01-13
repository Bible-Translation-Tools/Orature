package org.wycliffeassociates.otter.common.domain.audio

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.narration.AudioFileUtils
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class AudioGenerator @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val concatAudio: ConcatenateAudio,
    private val audioUtils: AudioFileUtils
) {

    fun convertTextToAudio(text: String): File {
        return generate(text)
    }

    fun convertTextToAudio(textItems: List<String>): File {
        return buildAudio(textItems)
    }

    private fun buildAudio(chunksText: List<String>): File {
        val maxToken = 4000
        var counter = 0
        val textItemsToConvert = mutableListOf<String>()
        var tokenPayload = ""

        chunksText.forEach {
            val newLength = counter + it.length + 1 // new length includes space
            if (newLength <= maxToken) {
                tokenPayload += " $it"
                counter = newLength
            } else {
                textItemsToConvert.add(tokenPayload)
                // reset payload with new string
                tokenPayload = it
                counter = it.length
            }
        }
        textItemsToConvert.add(tokenPayload)

        val audioFiles = textItemsToConvert.map { generate(it) }

        return concatAudio.execute(audioFiles, includeMarkers = false).blockingGet()
    }

    private fun generate(text: String): File {
        println("GENERATING...")
        val generated = File.createTempFile("temp-tts", ".mp3", directoryProvider.tempDirectory)
        request(text, generated)
        val outputFile = File.createTempFile("tts", ".mp3", directoryProvider.tempDirectory)
        audioUtils.resampleAudio(generated, outputFile)
        println("DONE!")
        return outputFile
    }

    private fun request(content: String, outputFile: File) {
        val apiUrl = "https://api.openai.com/v1/audio/speech"
        val apiKey = System.getenv("OPENAI_TTS_KEY")

        // Create an HttpClient instance
        val client = HttpClient.newHttpClient()

        val serializedContent = ObjectMapper(JsonFactory()).writeValueAsString(content)
        // Create the JSON payload
        val jsonPayload = """
        {
            "model": "tts-1",
            "input": $serializedContent,
            "voice": "shimmer"
        }
        """.trimIndent()
//        "response_format": "wav"

        // Create an HttpRequest instance
        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build()

        // Send the request and get the response
        val response = client.send(request, HttpResponse.BodyHandlers.ofFile(outputFile.toPath()))

        // Check if the response is successful
        if (response.statusCode() == 200) {
            println("Audio file saved as $outputFile")
        } else {
            println("Request failed with status code: ${response.statusCode()}")
        }
    }
}


