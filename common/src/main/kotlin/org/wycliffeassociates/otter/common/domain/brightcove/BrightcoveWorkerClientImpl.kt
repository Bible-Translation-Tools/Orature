/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.brightcove

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLEncoder
import javax.inject.Inject

class BrightcoveWorkerClientImpl @Inject constructor() : BrightcoveWorkerClient {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
    private val httpClient = OkHttpClient()

    override fun login(config: BrightcoveWorkerConfig): Single<BrightcoveWorkerAuthLoginResponse> {
        return Single.fromCallable {
            val url = "${config.workerBaseUrl}/auth/login"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            executeJson(request, BrightcoveWorkerAuthLoginResponse::class.java)
        }
    }

    override fun upload(
        config: BrightcoveWorkerConfig,
        token: String,
        uploadId: String,
        sourceFile: File
    ): Single<BrightcoveWorkerUploadResult> {
        return Single.fromCallable {
            val url = "${config.workerBaseUrl}/upload/${encode(uploadId)}/${encode(sourceFile.name)}"
            val mediaType = MediaType.parse(guessContentType(sourceFile))
            val body = RequestBody.create(mediaType, sourceFile)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .put(body)
                .build()
            executeJson(request, BrightcoveWorkerUploadResult::class.java)
        }.doOnError { e ->
            logger.error("Brightcove worker upload failed", e)
        }
    }

    override fun ingest(
        config: BrightcoveWorkerConfig,
        token: String,
        request: BrightcoveWorkerIngestRequest
    ): Single<BrightcoveWorkerIngestResult> {
        return Single.fromCallable {
            val url = "${config.workerBaseUrl}/ingest"
            val body = RequestBody.create(JSON, mapper.writeValueAsBytes(request))
            val httpRequest = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .post(body)
                .build()
            executeJson(httpRequest, BrightcoveWorkerIngestResult::class.java)
        }.doOnError { e ->
            logger.error("Brightcove worker ingest failed", e)
        }
    }

    private fun <T> executeJson(request: Request, clazz: Class<T>): T {
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val body = response.body()?.string()
                throw BrightcoveWorkerHttpException(response.code(), body)
            }
            val body = response.body()?.byteStream()
                ?: throw IllegalStateException("Worker response missing body")
            return mapper.readValue(body, clazz)
        }
    }

    private fun guessContentType(sourceFile: File): String {
        val name = sourceFile.name.lowercase()
        return when {
            name.endsWith(".mp3") -> "audio/mpeg"
            name.endsWith(".vtt") -> "text/vtt"
            else -> "application/octet-stream"
        }
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
    }

    companion object {
        private val JSON = MediaType.parse("application/json; charset=utf-8")
    }
}
