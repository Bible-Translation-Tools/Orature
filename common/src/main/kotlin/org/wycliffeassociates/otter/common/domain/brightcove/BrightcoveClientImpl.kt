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
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Inject
import java.io.File
import java.util.Base64

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

    private val uploadClient = OkHttpClient()

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
                        tags = request.tags,
                        customFields = request.customFields
                    )
                )
            }
            .map { it.id }
    }

    override fun uploadSource(
        config: BrightcoveConfig,
        videoId: String,
        sourceFile: File
    ): Single<BrightcoveUploadResult> {
        return getAccessToken(config)
            .flatMap { token ->
                ingestApi.requestUploadUrl(
                    bearer(token),
                    config.accountId,
                    videoId
                )
            }
            .flatMap { uploadInfo ->
                Single.fromCallable {
                    uploadToSignedUrl(uploadInfo, sourceFile)
                    BrightcoveUploadResult(masterUrl = uploadInfo.apiRequestUrl)
                }
            }
    }

    override fun ingest(
        config: BrightcoveConfig,
        videoId: String,
        masterUrl: String
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
                        callbacks = config.callbacks
                    )
                )
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
            .map { it.accessToken }
    }

    private fun uploadToSignedUrl(uploadInfo: BrightcoveUploadUrlResponse, file: File) {
        val mediaType = MediaType.parse("audio/mpeg")
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
        @JsonProperty("tags")
        val tags: List<String>,
        @JsonProperty("custom_fields")
        val customFields: Map<String, String>
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
        val callbacks: List<String>? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class BrightcoveIngestResponse(
        @JsonProperty("id")
        val id: String? = null,
        @JsonProperty("job_id")
        val jobId: String? = null
    )

    private interface BrightcoveDynamicIngestApi {
        @POST("/v1/accounts/{accountId}/videos/{videoId}/upload-urls")
        fun requestUploadUrl(
            @Header("Authorization") bearer: String,
            @Path("accountId") accountId: String,
            @Path("videoId") videoId: String
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
