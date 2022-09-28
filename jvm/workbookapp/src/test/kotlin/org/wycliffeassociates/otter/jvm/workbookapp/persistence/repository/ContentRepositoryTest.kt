/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repository

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.JooqTestConfiguration
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.ContentDao
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.ContentRepository
import java.io.File
import kotlin.io.path.createTempDirectory

class ContentRepositoryTest {
    private val databaseFile = createTempDirectory("otter-test").toFile().resolve("db.sqlite")
    private val schemaFile = File(
        javaClass.classLoader.getResource("sql/AppDatabaseSchema0.sql").file
    )
    private lateinit var db: AppDatabase

    private val content = listOf(
        Content(
            1,
            "chunk",
            1,
            2,
            null,
            null,
            null,
            ContentType.TEXT,
            1,
            id = 1
        ),
        Content(
            2,
            "chunk",
            2,
            3,
            null,
            null,
            null,
            ContentType.TEXT,
            1,
            id = 2
        )
    )

    private val sourceContents = listOf(
        Content(
            1,
            "verse",
            1,
            1,
            null,
            null,
            null,
            ContentType.TEXT,
            1,
            id = 1
        ),
        Content(
            2,
            "verse",
            2,
            2,
            null,
            null,
            null,
            ContentType.TEXT,
            1,
            id = 2
        ),
        Content(
            3,
            "verse",
            3,
            3,
            null,
            null,
            null,
            ContentType.TEXT,
            1,
            id = 3
        )
    )

    private val derivedToSourceLinks = listOf(
        Pair(content[0].id, sourceContents[0].id),
        Pair(content[0].id, sourceContents[1].id),
        Pair(content[1].id, sourceContents[1].id),
        Pair(content[1].id, sourceContents[2].id),
    )

    @After
    fun cleanUp() {
        db.close()
        databaseFile.parentFile.deleteRecursively()
    }

    @Before
    fun setup() {
        JooqTestConfiguration.createDatabase(databaseFile.path, schemaFile)
        db = AppDatabase(databaseFile)
    }

    @Test
    fun testLinkDerivedToSource() {
        val mockDb = spy(db)
        val mockContentDao = mock<ContentDao> {
            on { linkDerivative(any(), any(), any()) } doAnswer {  }
        }
        `when`(mockDb.contentDao).doReturn(mockContentDao)
        val repository: IContentRepository = ContentRepository(mockDb)

        verify(mockContentDao, never()).linkDerivative(any(),any(),any())

        repository.linkDerivedToSource(content, sourceContents)
            .blockingAwait()

        // capture arguments passed into mocked method for verification
        val contentIdsCaptor = ArgumentCaptor.forClass(Int::class.java)
        val sourceIdsCaptor = ArgumentCaptor.forClass(Int::class.java)

        verify(mockContentDao, times(derivedToSourceLinks.size))
            .linkDerivative(contentIdsCaptor.capture(), sourceIdsCaptor.capture(), anyOrNull())

        val contentIds = contentIdsCaptor.allValues
        val sourceIds = sourceIdsCaptor.allValues

        // verify each link (contentFK - sourceFK) must match sample
        derivedToSourceLinks.forEachIndexed { i, pair ->
            Assert.assertEquals(pair.first, contentIds[i])
            Assert.assertEquals(pair.second, sourceIds[i])
        }
    }
}