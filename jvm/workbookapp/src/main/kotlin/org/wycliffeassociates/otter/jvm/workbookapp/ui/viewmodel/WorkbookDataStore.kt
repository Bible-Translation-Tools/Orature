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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Resource
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import tornadofx.*
import java.text.MessageFormat
import java.util.concurrent.Callable

class WorkbookDataStore : Component(), ScopedInstance {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val activeWorkbookProperty = SimpleObjectProperty<Workbook>()
    val workbook: Workbook
        get() = activeWorkbookProperty.value ?: throw IllegalStateException("Workbook is null")

    val currentModeProperty = SimpleObjectProperty<ProjectMode>()

    val activeChapterProperty = SimpleObjectProperty<Chapter>()
    val chapter: Chapter
        get() = activeChapterProperty.value ?: throw IllegalStateException("Chapter is null")
    val workbookRecentChapterMap = mutableMapOf<Int, Int>()

    val activeChunkProperty = SimpleObjectProperty<Chunk>()
    val chunk: Chunk? by activeChunkProperty

    val activeResourceComponentProperty = SimpleObjectProperty<Resource.Component>()
    val activeResourceComponent by activeResourceComponentProperty
    val activeResourceProperty = SimpleObjectProperty<Resource>()

    val activeTakeNumberProperty = SimpleIntegerProperty()
    val sourceLicenseProperty = SimpleStringProperty()
    val sourceInfoProperty = SimpleStringProperty()

    init {
        activeWorkbookProperty.onChange {
            logger.info("Active workbook: ${it?.target?.slug}")
            if (it == null) {
                activeChapterProperty.set(null)
                activeChunkProperty.set(null)
            } else {
                sourceLicenseProperty.set(it.source.resourceMetadata.license)
                sourceInfoProperty.set(
                    MessageFormat.format(
                        messages["source_info_title"],
                        it.source.language.name,
                        it.source.resourceMetadata.title
                    )
                )
            }
        }
    }

    fun getSourceText(): Maybe<String> {
        return when {
            activeResourceComponent != null -> Maybe.just(
                activeResourceComponent.textItem.text
            )
            chunk != null -> getChunkSourceText()
            else -> getSourceChapter().map { _chapter ->
                val verses = workbook.projectFilesAccessor.getChapterText(workbook.source.slug, _chapter.sort)
                combineVerses(verses)
            }
        }
    }

    private fun getChunkSourceText(): Maybe<String> {
        return Maybe
            .fromCallable {
                chunk?.let { chunk ->
                    val verses = workbook.projectFilesAccessor.getChunkText(
                        workbook.source.slug,
                        chapter.sort,
                        chunk.start,
                        chunk.end
                    )
                    val text = combineVerses(verses)
                    text
                } ?: ""
            }
            .subscribeOn(Schedulers.io())
    }

    private fun combineVerses(verses: List<String>): String {
        return StringBuilder().apply { verses.forEach { append("$it\n") } }.toString()
    }

    fun sourceTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                activeChapterProperty.value?.let {
                    getSourceText().blockingGet() ?: ""
                }
            },
            activeChapterProperty,
            activeChunkProperty,
            activeResourceComponentProperty
        )
    }

    fun activeChapterTitleBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                activeChapterProperty.value?.let { chapter ->
                    MessageFormat.format(
                        messages["chapterTitle"],
                        messages[chapter.label],
                        activeChapterProperty.value.title,
                    )
                }
            },
            activeWorkbookProperty,
            activeChapterProperty
        )
    }

    fun activeChunkTitleBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                if (activeWorkbookProperty.value != null && activeChapterProperty.value != null) {
                    if (activeChunkProperty.value != null) {
                        MessageFormat.format(
                            messages["chunkTitle"],
                            messages[activeChunkProperty.value.label],
                            activeChunkProperty.value.sort
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            },
            activeWorkbookProperty,
            activeChapterProperty,
            activeChunkProperty
        )
    }

    fun activeTitleBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                if (activeWorkbookProperty.value != null && activeChapterProperty.value != null) {
                    if (activeChunkProperty.value != null) {
                        MessageFormat.format(
                            messages["bookChapterChunkTitle"],
                            activeWorkbookProperty.value.source.title,
                            activeChapterProperty.value.title,
                            activeChunkProperty.value.title
                        )
                    } else {
                        MessageFormat.format(
                            messages["bookChapterTitle"],
                            activeWorkbookProperty.value.source.title,
                            activeChapterProperty.value.title
                        )
                    }
                } else {
                    null
                }
            },
            activeWorkbookProperty,
            activeChapterProperty,
            activeChunkProperty
        )
    }

    fun getSourceChapter(): Maybe<Chapter> {
        return workbook
            .source
            .chapters
            .filter {
                it.title == chapter.title
            }
            .singleElement()
    }

    fun getSourceChunk(): Maybe<Chunk> {
        return getSourceChapter()
            .map { _chapter ->
                _chapter
                    .chunks
                    .value
                    ?.find { _chunk ->
                        _chunk.sort == chunk?.sort
                    }
            }
    }
}
