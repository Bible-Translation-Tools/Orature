/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain

import io.reactivex.Completable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.domain.plugins.ImportAudioPlugins
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File

class ImportAudioPluginsTest {
    val mockPluginRegistrar = Mockito.mock(IAudioPluginRegistrar::class.java)
    val mockDirectoryProvider = Mockito.mock(IDirectoryProvider::class.java)

    // Required in Kotlin to use Mockito any() argument matcher
    fun <T> helperAny(): T {
        return ArgumentMatchers.any()
    }

    @Before
    fun setup() {
        // Configure the mock plugin registrar
        Mockito
                .`when`(mockPluginRegistrar.import(helperAny()))
                .thenReturn(Completable.complete())
        Mockito
                .`when`(mockPluginRegistrar.importAll(helperAny()))
                .thenReturn(Completable.complete())

        // Configure the mock directory provider
        Mockito.`when`(mockDirectoryProvider.audioPluginDirectory)
                .thenReturn(
                        File(
                                ImportAudioPluginsTest::class.java
                                        .classLoader
                                        .getResource("plugins")
                                        .toURI()
                                        .path
                        )
                )
    }

    @Test
    fun testImportAll() {
        // Expect completable to finish
        ImportAudioPlugins(mockPluginRegistrar, mockDirectoryProvider)
                .importAll()
                .blockingAwait()
    }

    @Test
    fun testImportExternal() {
        // Input file
        val inputFile = File(ImportAudioPluginsTest::class.java
                .classLoader
                .getResource("audacity.yaml")
                .toURI()
                .path
        )
        // Expected output file location
        val expectedOutputFile = mockDirectoryProvider.audioPluginDirectory.resolve("audacity.yaml")

        // Expect completable to finish and file to be copied to new directory
        ImportAudioPlugins(mockPluginRegistrar, mockDirectoryProvider)
                .importExternal(inputFile)
                .blockingAwait()

        Assert.assertTrue(expectedOutputFile.exists())

        // Tear down
        expectedOutputFile.delete()
    }
}