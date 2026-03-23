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
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface BrightcoveWorkerAuthService {
    fun getAccessToken(config: BrightcoveWorkerConfig): Single<String>
}

class BrightcoveWorkerAuthServiceImpl @Inject constructor(
    private val client: BrightcoveWorkerClient,
    private val store: BrightcoveWorkerAuthStore,
    private val browserOpener: ExternalBrowserOpener
) : BrightcoveWorkerAuthService {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()

    override fun getAccessToken(config: BrightcoveWorkerConfig): Single<String> {
        val cached = store.load()
        if (cached != null && !isExpired(cached)) {
            return Single.just(cached.token)
        }

        val loginUrl = "${config.workerBaseUrl}/auth/login"
        browserOpener.open(loginUrl)

        return pollForToken(config)
            .doOnSuccess { token ->
                store.save(token)
            }
            .map { it.token }
    }

    private fun pollForToken(config: BrightcoveWorkerConfig): Single<BrightcoveWorkerAccessToken> {
        return io.reactivex.Observable.interval(0, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
            .flatMapSingle {
                client.login(config)
                    .map { response ->
                        BrightcoveWorkerAccessToken(
                            token = response.token,
                            expiresAtEpochSeconds = decodeJwtExpiry(response.token)
                        )
                    }
                    .onErrorResumeNext { e ->
                        if (isUnauthorized(e)) {
                            Single.just<BrightcoveWorkerAccessToken?>(null)
                        } else {
                            Single.error(e)
                        }
                    }
            }
            .filter { token -> token != null }
            .firstOrError()
            .map { token -> token!! }
            .timeout(AUTH_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .doOnError { e ->
                logger.error("Brightcove worker auth failed", e)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun isUnauthorized(error: Throwable): Boolean {
        return error is BrightcoveWorkerHttpException && error.statusCode == 401
    }

    private fun decodeJwtExpiry(token: String): Long? {
        val parts = token.split(".")
        if (parts.size < 2) return null
        return try {
            val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
            val tree = mapper.readTree(payloadJson)
            tree.path("exp").asLong(0L).takeIf { it > 0L }
        } catch (_: Exception) {
            null
        }
    }

    private fun isExpired(token: BrightcoveWorkerAccessToken): Boolean {
        val expiresAt = token.expiresAtEpochSeconds ?: return false
        return Instant.now().epochSecond >= expiresAt
    }

    companion object {
        private const val POLL_INTERVAL_MS = 1500L
        private const val AUTH_TIMEOUT_MS = 120_000L
    }
}
