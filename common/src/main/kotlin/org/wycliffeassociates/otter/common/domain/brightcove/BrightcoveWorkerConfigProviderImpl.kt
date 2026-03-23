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
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.FileNotFoundException
import javax.inject.Inject

class BrightcoveWorkerConfigProviderImpl @Inject constructor(
    private val directoryProvider: IDirectoryProvider
) : BrightcoveWorkerConfigProvider {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun load(): Single<BrightcoveWorkerConfig> {
        return Single.fromCallable {
            val configDir = directoryProvider.getAppDataDirectory("config")
            val configFile = configDir.resolve("brightcove-worker.json")

            if (!configFile.exists()) {
                throw FileNotFoundException("Brightcove worker config file not found: ${configFile.absolutePath}")
            }

            val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
            val raw = mapper.readValue(configFile, BrightcoveWorkerConfigFile::class.java)

            val workerUrl = envOr(raw.workerUrl, ENV_WORKER_URL)
            validateRequired("worker_url", workerUrl)

            BrightcoveWorkerConfig(
                workerBaseUrl = workerUrl!!.trimEnd('/'),
                ingestProfile = raw.ingestProfile,
                callbacks = raw.callbacks
            )
        }.doOnError { e ->
            logger.error("Error loading Brightcove worker config", e)
        }
    }

    private fun envOr(value: String?, key: String): String? {
        val envValue = System.getenv(key)?.trim()?.takeIf { it.isNotEmpty() }
        return envValue ?: value
    }

    private fun validateRequired(field: String, value: String?) {
        if (value.isNullOrBlank()) {
            throw IllegalArgumentException("Brightcove worker config missing required field: $field")
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class BrightcoveWorkerConfigFile(
        @JsonProperty("worker_url")
        @JsonAlias("workerUrl", "worker_base_url", "workerBaseUrl")
        val workerUrl: String? = null,
        @JsonProperty("ingest_profile")
        @JsonAlias("ingestProfile")
        val ingestProfile: String? = null,
        @JsonProperty("callbacks")
        val callbacks: List<String>? = null
    )

    companion object {
        private const val ENV_WORKER_URL = "BRIGHTCOVE_WORKER_URL"
    }
}
