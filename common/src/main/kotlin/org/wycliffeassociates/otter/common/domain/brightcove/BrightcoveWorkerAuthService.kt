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

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
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

    override fun getAccessToken(config: BrightcoveWorkerConfig): Single<String> {
        store.clear()

        return client.startAuth(config)
            .flatMap { start ->
                browserOpener.open(start.loginUrl)
                pollForToken(config, start.state)
            }
            .doOnSuccess { token ->
                store.save(token)
            }
            .map { it.token }
    }

    private fun pollForToken(
        config: BrightcoveWorkerConfig,
        state: String
    ): Single<BrightcoveWorkerAccessToken> {
        return io.reactivex.Observable.interval(0, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
            .flatMapSingle {
                client.pollAuth(config, state)
            }
            .filter { response ->
                response.status.equals("ready", ignoreCase = true) && !response.token.isNullOrBlank()
            }
            .firstOrError()
            .map { response ->
                BrightcoveWorkerAccessToken(
                    token = response.token!!,
                    sessionId = state,
                    expiresAtEpochSeconds = response.expiresAtEpochSeconds
                )
            }
            .timeout(AUTH_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .doOnError { e ->
                logger.error("Brightcove worker auth failed", e)
            }
            .subscribeOn(Schedulers.io())
    }

    companion object {
        private const val POLL_INTERVAL_MS = 1500L
        private const val AUTH_TIMEOUT_MS = 120_000L
    }
}
