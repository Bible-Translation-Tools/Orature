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
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import javax.inject.Inject

class BrightcoveWorkerAuthStoreImpl @Inject constructor(
    private val directoryProvider: IDirectoryProvider
) : BrightcoveWorkerAuthStore {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()

    override fun load(): BrightcoveWorkerAccessToken? {
        val configDir = directoryProvider.getAppDataDirectory("config")
        val tokenFile = configDir.resolve("brightcove-auth.json")
        if (!tokenFile.exists() || tokenFile.length() == 0L) {
            return null
        }
        return try {
            mapper.readValue(tokenFile, BrightcoveWorkerAccessToken::class.java)
        } catch (e: Exception) {
            logger.warn("Failed to read Brightcove worker auth token; ignoring", e)
            null
        }
    }

    override fun save(token: BrightcoveWorkerAccessToken) {
        val configDir = directoryProvider.getAppDataDirectory("config")
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
        val tokenFile = configDir.resolve("brightcove-auth.json")
        mapper.writeValue(tokenFile, token)
    }

    override fun clear() {
        val configDir = directoryProvider.getAppDataDirectory("config")
        val tokenFile = configDir.resolve("brightcove-auth.json")
        if (tokenFile.exists()) {
            tokenFile.delete()
        }
    }
}
