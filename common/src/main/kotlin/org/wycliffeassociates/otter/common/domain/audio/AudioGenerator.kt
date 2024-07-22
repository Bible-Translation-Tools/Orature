package org.wycliffeassociates.otter.common.domain.audio

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.inject.Inject

class AudioGenerator @Inject constructor(
    private val directoryProvider: IDirectoryProvider
) {

    fun convertTextToAudio(text: String): File {
        val outputFile = File.createTempFile("tts", ".mp3", directoryProvider.tempDirectory)
        request(text, outputFile)
        return outputFile
    }

    private fun request(content: String, outputFile: File) {
        val apiUrl = ""
        val apiKey = "" // Replace with your actual API key

        // Create an HttpClient instance
        val client = HttpClient.newHttpClient()

        val serializedContent = ObjectMapper(JsonFactory()).writeValueAsString(content)
        // Create the JSON payload
        val jsonPayload = """
        {
            "model": "tts-1",
            "input": $serializedContent,
            "voice": "echo"
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


