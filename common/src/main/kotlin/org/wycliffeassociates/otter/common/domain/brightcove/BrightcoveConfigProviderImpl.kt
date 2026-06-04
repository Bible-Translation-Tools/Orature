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

class BrightcoveConfigProviderImpl @Inject constructor(
    private val directoryProvider: IDirectoryProvider
) : BrightcoveConfigProvider {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun load(): Single<BrightcoveConfig> {
        return Single.fromCallable {
            val configDir = directoryProvider.getAppDataDirectory("config")
            val configFile = configDir.resolve("brightcove.json")

            if (!configFile.exists()) {
                throw FileNotFoundException("Brightcove config file not found: ${configFile.absolutePath}")
            }

            val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
            val raw = mapper.readValue(configFile, BrightcoveConfigFile::class.java)

            val accountId = envOr(raw.accountId, ENV_ACCOUNT_ID)
            val clientId = envOr(raw.clientId, ENV_CLIENT_ID)
            val clientSecret = envOr(raw.clientSecret, ENV_CLIENT_SECRET)
            val ingestProfile = envOr(raw.ingestProfile, ENV_INGEST_PROFILE)
            val callbacks = raw.callbacks

            validateRequired("account_id", accountId)
            validateRequired("client_id", clientId)
            validateRequired("client_secret", clientSecret)

            BrightcoveConfig(
                accountId = accountId!!,
                clientId = clientId!!,
                clientSecret = clientSecret!!,
                ingestProfile = ingestProfile,
                callbacks = callbacks
            )
        }.doOnError { e ->
            logger.error("Error loading Brightcove config", e)
        }
    }

    private fun envOr(value: String?, key: String): String? {
        val envValue = System.getenv(key)?.trim()?.takeIf { it.isNotEmpty() }
        return envValue ?: value
    }

    private fun validateRequired(field: String, value: String?) {
        if (value.isNullOrBlank()) {
            throw IllegalArgumentException("Brightcove config missing required field: $field")
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class BrightcoveConfigFile(
        @JsonProperty("account_id")
        @JsonAlias("accountId")
        val accountId: String? = null,
        @JsonProperty("client_id")
        @JsonAlias("clientId")
        val clientId: String? = null,
        @JsonProperty("client_secret")
        @JsonAlias("clientSecret")
        val clientSecret: String? = null,
        @JsonProperty("ingest_profile")
        @JsonAlias("ingestProfile")
        val ingestProfile: String? = null,
        @JsonProperty("callbacks")
        val callbacks: List<String>? = null
    )

    companion object {
        private const val ENV_ACCOUNT_ID = "BRIGHTCOVE_ACCOUNT_ID"
        private const val ENV_CLIENT_ID = "BRIGHTCOVE_CLIENT_ID"
        private const val ENV_CLIENT_SECRET = "BRIGHTCOVE_CLIENT_SECRET"
        private const val ENV_INGEST_PROFILE = "BRIGHTCOVE_INGEST_PROFILE"
    }
}
