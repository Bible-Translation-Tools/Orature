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
    internal var envLookup: (String) -> String? = System::getenv

    override fun load(): Single<BrightcoveWorkerConfig> {
        return Single.fromCallable {
            val configDir = directoryProvider.getAppDataDirectory("config")
            val configFile = resolveConfigFile(configDir)
            val fileExists = configFile.exists()
            val raw = if (fileExists) {
                val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
                mapper.readValue(configFile, BrightcoveWorkerConfigFile::class.java)
            } else {
                BrightcoveWorkerConfigFile()
            }

            val proxyUrl = envOr(raw.proxyUrl, ENV_PROXY_URL)
            if (!fileExists && proxyUrl.isNullOrBlank()) {
                throw FileNotFoundException("Brightcove worker config file not found: ${configFile.absolutePath}")
            }

            validateRequired("brightcove_proxy_url", proxyUrl)

            BrightcoveWorkerConfig(
                workerBaseUrl = proxyUrl!!.trimEnd('/'),
                ingestProfile = raw.ingestProfile,
                callbacks = raw.callbacks
            )
        }.doOnError { e ->
            logger.error("Error loading Brightcove worker config", e)
        }
    }

    override fun isAvailable(): Boolean {
        val configDir = directoryProvider.getAppDataDirectory("config")
        return !envLookup(ENV_PROXY_URL).isNullOrBlank() || resolveConfigFile(configDir).exists()
    }

    private fun envOr(value: String?, vararg keys: String): String? {
        val envValue = keys
            .asSequence()
            .mapNotNull { envLookup(it)?.trim()?.takeIf { candidate -> candidate.isNotEmpty() } }
            .firstOrNull()
        return envValue ?: value
    }

    private fun validateRequired(field: String, value: String?) {
        if (value.isNullOrBlank()) {
            throw IllegalArgumentException("Brightcove worker config missing required field: $field")
        }
    }

    private fun resolveConfigFile(configDir: java.io.File): java.io.File {
        return configDir.resolve(CONFIG_FILE)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class BrightcoveWorkerConfigFile(
        @JsonProperty("brightcove_proxy_url")
        val proxyUrl: String? = null,
        @JsonProperty("ingest_profile")
        val ingestProfile: String? = null,
        @JsonProperty("callbacks")
        val callbacks: List<String>? = null
    )

    companion object {
        private const val CONFIG_FILE = "brightcove_proxy.json"
        private const val ENV_PROXY_URL = "BRIGHTCOVE_PROXY_URL"
    }
}
