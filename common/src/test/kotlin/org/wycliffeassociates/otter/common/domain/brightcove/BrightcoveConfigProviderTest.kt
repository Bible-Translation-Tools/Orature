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

import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import kotlin.io.path.createTempDirectory

class BrightcoveConfigProviderTest {

    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = createTempDirectory("brightcove-config-test").toFile()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun loadValidConfig() {
        val configDir = tempDir.resolve("config").apply { mkdirs() }
        val configFile = configDir.resolve("brightcove.json")
        configFile.writeText(
            """
            {
              "account_id": "acct",
              "client_id": "client",
              "client_secret": "secret",
              "ingest_profile": "audio-profile",
              "callbacks": ["https://example.com/callback"]
            }
            """.trimIndent()
        )

        val directoryProvider = mockk<IDirectoryProvider>()
        every { directoryProvider.getAppDataDirectory("config") } returns configDir

        val provider = BrightcoveConfigProviderImpl(directoryProvider)
        val config = provider.load().blockingGet()

        assertEquals("acct", config.accountId)
        assertEquals("client", config.clientId)
        assertEquals("secret", config.clientSecret)
        assertEquals("audio-profile", config.ingestProfile)
        assertEquals(listOf("https://example.com/callback"), config.callbacks)
    }

    @Test
    fun missingConfigFileFails() {
        val configDir = tempDir.resolve("config").apply { mkdirs() }
        val directoryProvider = mockk<IDirectoryProvider>()
        every { directoryProvider.getAppDataDirectory("config") } returns configDir

        val provider = BrightcoveConfigProviderImpl(directoryProvider)

        try {
            provider.load().blockingGet()
            fail("Expected config load to fail")
        } catch (e: Exception) {
            assertTrue(e.cause is java.io.FileNotFoundException)
        }
    }

    @Test
    fun missingRequiredFieldFails() {
        val configDir = tempDir.resolve("config").apply { mkdirs() }
        val configFile = configDir.resolve("brightcove.json")
        configFile.writeText(
            """
            {
              "account_id": "acct",
              "client_id": "client"
            }
            """.trimIndent()
        )

        val directoryProvider = mockk<IDirectoryProvider>()
        every { directoryProvider.getAppDataDirectory("config") } returns configDir

        val provider = BrightcoveConfigProviderImpl(directoryProvider)

        try {
            provider.load().blockingGet()
            fail("Expected config load to fail")
        } catch (e: Exception) {
            assertTrue(e is IllegalArgumentException || e.cause is IllegalArgumentException)
        }
    }
}
