package org.wycliffeassociates.otter.common.domain.audio

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException


class MarkerGenerator {

    fun generate(audioFile: File) {
        println(request(audioFile))
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
//            .addHeader(
//                "Cookie",
//                "__cf_bm=7jQ2gZo3uBHVLHT2Fu1E3NUpXcwv0LJ90oPYRgkPkVM-1736881337-1.0.1.1-6KjrL_iks6Brs0k6PBm45.3.Wf_f.Zt54y9yEZQHlJBTn8jY6MHMkvjxip_pXkKo0ALb4u6N4PWMBGueuC7gBQ; _cfuvid=55Zl2jAEbWNJ5OKBT8iBzoQAkJhaugCsbZLxds5L1Ds-1736881337971-0.0.1.1-604800000"
//            )
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
}

@JsonIgnoreProperties(ignoreUnknown=true)
private data class OpenAITranscription(
    val task: String,
    val language: String,
    val duration: Double,
    val text: String,
    val words: List<Word>
)

private data class Word(
    val word: String,
    val start: Double,
    val end: Double
)
