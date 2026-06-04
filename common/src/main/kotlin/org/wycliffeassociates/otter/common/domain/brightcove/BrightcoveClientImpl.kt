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

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.HttpException
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import java.io.File
import java.util.Base64
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class BrightcoveClientImpl @Inject constructor() : BrightcoveClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()

    private val retrofitOauth = Retrofit.Builder()
        .baseUrl(OAUTH_BASE_URL)
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    private val retrofitCms = Retrofit.Builder()
        .baseUrl(CMS_BASE_URL)
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    private val retrofitIngest = Retrofit.Builder()
        .baseUrl(INGEST_BASE_URL)
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    private val oauthApi = retrofitOauth.create(BrightcoveOAuthApi::class.java)
    private val cmsApi = retrofitCms.create(BrightcoveCmsApi::class.java)
    private val ingestApi = retrofitIngest.create(BrightcoveDynamicIngestApi::class.java)

    private val uploadClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .callTimeout(5, TimeUnit.MINUTES)
        .build()

    override fun findVideoIdByReferenceId(
        config: BrightcoveConfig,
        referenceId: String
    ): Maybe<String> {
        val trimmedReference = referenceId.trim()
        if (trimmedReference.isEmpty()) {
            return Maybe.empty()
        }
        return getAccessToken(config)
            .flatMap { token ->
                cmsApi.listVideos(
                    bearer(token),
                    config.accountId,
                    limit = 1,
                    query = "reference_id:$trimmedReference"
                )
            }
            .doOnError { error ->
                logHttpError("list videos", error)
            }
            .flatMapMaybe { items ->
                val id = items.firstOrNull()?.id
                if (id.isNullOrBlank()) {
                    Maybe.empty()
                } else {
                    Maybe.just(id)
                }
            }
    }

    override fun createVideo(
        config: BrightcoveConfig,
        request: BrightcoveVideoRequest
    ): Single<String> {
        return getAccessToken(config)
            .flatMap { token ->
                cmsApi.createVideo(
                    bearer(token),
                    config.accountId,
                    CmsCreateVideoRequest(
                        name = request.name,
                        referenceId = request.referenceId,
                        tags = request.tags,
                        customFields = request.customFields,
                        cuePoints = request.cuePoints.takeIf { it.isNotEmpty() }
                    )
                )
            }
            .map { it.id }
            .onErrorResumeNext { error ->
                logHttpError("create video", error)
                val httpError = error as? HttpException

                if (httpError?.code() == 422) {
                    return@onErrorResumeNext findVideoIdByReferenceId(config, request.referenceId)
                        .switchIfEmpty(Maybe.error(error))
                        .toSingle()
                }

                Single.error(error)
            }
    }

    override fun uploadSource(
        config: BrightcoveConfig,
        videoId: String,
        sourceFile: File
    ): Single<BrightcoveUploadResult> {
        val sourceName = encodeSourceName(sourceFile.name)
        return getAccessToken(config)
            .flatMap { token ->
                ingestApi.requestUploadUrl(
                    bearer(token),
                    config.accountId,
                    videoId,
                    sourceName
                )
            }
            .doOnError { error ->
                logHttpError("request upload url", error)
            }
            .flatMap { uploadInfo ->
                Single.fromCallable {
                    uploadToSignedUrl(uploadInfo, sourceFile)
                    BrightcoveUploadResult(masterUrl = uploadInfo.apiRequestUrl)
                }
            }
    }

    override fun updateCuePoints(
        config: BrightcoveConfig,
        videoId: String,
        cuePoints: List<BrightcoveCuePoint>
    ): Completable {
        if (cuePoints.isEmpty()) {
            return Completable.complete()
        }
        return getAccessToken(config)
            .flatMapCompletable { token ->
                cmsApi.updateVideo(
                    bearer(token),
                    config.accountId,
                    videoId,
                    CmsUpdateVideoRequest(cuePoints = cuePoints)
                )
            }
            .doOnError { error ->
                logHttpError("update cue points", error)
            }
    }

    override fun ingest(
        config: BrightcoveConfig,
        videoId: String,
        masterUrl: String,
        textTracks: List<BrightcoveTextTrack>
    ): Single<BrightcoveIngestResult> {
        return getAccessToken(config)
            .flatMap { token ->
                ingestApi.ingest(
                    bearer(token),
                    config.accountId,
                    videoId,
                    BrightcoveIngestRequest(
                        master = BrightcoveIngestMaster(url = masterUrl),
                        profile = config.ingestProfile,
                        callbacks = config.callbacks,
                        textTracks = textTracks.takeIf { it.isNotEmpty() }
                    )
                )
            }
            .doOnError { error ->
                logHttpError("ingest", error)
            }
            .map { response ->
                BrightcoveIngestResult(
                    jobId = response.jobId ?: response.id
                )
            }
    }

    private fun getAccessToken(config: BrightcoveConfig): Single<String> {
        val credentials = "${config.clientId}:${config.clientSecret}"
        val encoded = Base64.getEncoder().encodeToString(credentials.toByteArray())
        val authHeader = "Basic $encoded"

        return oauthApi.getAccessToken(authHeader)
            .doOnError { error ->
                logHttpError("oauth token", error)
            }
            .map { it.accessToken }
    }

    private fun uploadToSignedUrl(uploadInfo: BrightcoveUploadUrlResponse, file: File) {
        val mediaType = MediaType.parse(contentTypeFor(file))
        val requestBody = RequestBody.create(mediaType, file)
        val requestBuilder = Request.Builder()
            .url(uploadInfo.signedUrl)
            .put(requestBody)

        uploadInfo.headers?.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        val response = uploadClient.newCall(requestBuilder.build()).execute()
        response.use {
            if (!it.isSuccessful) {
                val body = it.body()?.string()
                logger.error("Brightcove upload failed: ${it.code()} ${it.message()} $body")
                throw IllegalStateException("Brightcove upload failed with HTTP ${it.code()}")
            }
        }
    }

    private fun bearer(token: String) = "Bearer $token"

    private fun contentTypeFor(file: File): String {
        return when (file.extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "vtt" -> "text/vtt"
            else -> "application/octet-stream"
        }
    }

    private fun encodeSourceName(sourceName: String): String {
        return URLEncoder.encode(sourceName, Charsets.UTF_8.name())
            .replace("+", "%20")
    }

    private fun logHttpError(action: String, error: Throwable) {
        val httpError = error as? HttpException ?: return
        val body = try {
            httpError.response()?.errorBody()?.string()
        } catch (e: Exception) {
            null
        }
        if (!body.isNullOrBlank()) {
            logger.error("Brightcove $action failed: HTTP ${httpError.code()} $body")
        } else {
            logger.error("Brightcove $action failed: HTTP ${httpError.code()}", httpError)
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class OAuthTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("expires_in")
        val expiresIn: Long? = null
    )

    private interface BrightcoveOAuthApi {
        @FormUrlEncoded
        @POST("/v4/access_token")
        fun getAccessToken(
            @Header("Authorization") authHeader: String,
            @Field("grant_type") grantType: String = "client_credentials"
        ): Single<OAuthTokenResponse>
    }

    private data class CmsCreateVideoRequest(
        @JsonProperty("name")
        val name: String,
        @JsonProperty("reference_id")
        val referenceId: String,
        @JsonProperty("tags")
        val tags: List<String>,
        @JsonProperty("custom_fields")
        val customFields: Map<String, String>,
        @JsonProperty("cue_points")
        val cuePoints: List<BrightcoveCuePoint>? = null
    )

    private data class CmsUpdateVideoRequest(
        @JsonProperty("cue_points")
        val cuePoints: List<BrightcoveCuePoint>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class CmsVideoResponse(
        @JsonProperty("id")
        val id: String
    )

    private interface BrightcoveCmsApi {
        @POST("/v1/accounts/{accountId}/videos")
        fun createVideo(
            @Header("Authorization") bearer: String,
            @Path("accountId") accountId: String,
            @Body body: CmsCreateVideoRequest
        ): Single<CmsVideoResponse>

        @PATCH("/v1/accounts/{accountId}/videos/{videoId}")
        fun updateVideo(
            @Header("Authorization") bearer: String,
            @Path("accountId") accountId: String,
            @Path("videoId") videoId: String,
            @Body body: CmsUpdateVideoRequest
        ): Completable

        @GET("/v1/accounts/{accountId}/videos")
        fun listVideos(
            @Header("Authorization") bearer: String,
            @Path("accountId") accountId: String,
            @Query("limit") limit: Int,
            @Query("q") query: String
        ): Single<List<CmsVideoResponse>>
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class BrightcoveUploadUrlResponse(
        @JsonProperty("signed_url")
        @JsonAlias("signedUrl")
        val signedUrl: String,
        @JsonProperty("api_request_url")
        @JsonAlias("apiRequestUrl")
        val apiRequestUrl: String,
        @JsonProperty("headers")
        val headers: Map<String, String>? = null
    )

    private data class BrightcoveIngestMaster(
        @JsonProperty("url")
        val url: String
    )

    private data class BrightcoveIngestRequest(
        @JsonProperty("master")
        val master: BrightcoveIngestMaster,
        @JsonProperty("profile")
        val profile: String? = null,
        @JsonProperty("callbacks")
        val callbacks: List<String>? = null,
        @JsonProperty("text_tracks")
        val textTracks: List<BrightcoveTextTrack>? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class BrightcoveIngestResponse(
        @JsonProperty("id")
        val id: String? = null,
        @JsonProperty("job_id")
        val jobId: String? = null
    )

    private interface BrightcoveDynamicIngestApi {
        @GET("/v1/accounts/{accountId}/videos/{videoId}/upload-urls/{sourceName}")
        fun requestUploadUrl(
            @Header("Authorization") bearer: String,
            @Path("accountId") accountId: String,
            @Path("videoId") videoId: String,
            @Path("sourceName", encoded = true) sourceName: String
        ): Single<BrightcoveUploadUrlResponse>

        @POST("/v1/accounts/{accountId}/videos/{videoId}/ingest-requests")
        fun ingest(
            @Header("Authorization") bearer: String,
            @Path("accountId") accountId: String,
            @Path("videoId") videoId: String,
            @Body body: BrightcoveIngestRequest
        ): Single<BrightcoveIngestResponse>
    }

    companion object {
        private const val OAUTH_BASE_URL = "https://oauth.brightcove.com"
        private const val CMS_BASE_URL = "https://cms.api.brightcove.com"
        private const val INGEST_BASE_URL = "https://ingest.api.brightcove.com"
    }
}
