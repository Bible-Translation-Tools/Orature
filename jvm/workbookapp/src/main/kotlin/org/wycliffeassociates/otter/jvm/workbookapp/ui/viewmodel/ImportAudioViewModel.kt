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

import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.audio.AudioGenerator
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDescriptorRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import java.io.File
import javax.inject.Inject

class ImportAudioViewModel : ViewModel() {

    @Inject
    lateinit var audioGenerator: AudioGenerator

    @Inject
    lateinit var workbookRepository: IWorkbookRepository

    @Inject
    lateinit var workbookDescriptorRepository: IWorkbookDescriptorRepository

    @Inject
    lateinit var collectionRepository: ICollectionRepository

    @Inject
    lateinit var contentRepository: IContentRepository

    private val logger = LoggerFactory.getLogger(ImportAudioViewModel::class.java)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    val workbookDS: WorkbookDataStore by inject()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun isValidImportFile(files: List<File>): Boolean {
        return when {
            files.size > 1 -> {
                snackBarObservable.onNext(messages["importMultipleError"])
                logger.error(
                    "(Drag-Drop) Multi-files import is not supported. Input files: $files"
                )
                false
            }

            files.first().isDirectory -> {
                snackBarObservable.onNext(messages["importDirectoryError"])
                logger.error(
                    "(Drag-Drop) Directory import is not supported. Input path: ${files.first()}"
                )
                false
            }


            !AudioFileFormat.isSupported(files.first().extension) -> {
                snackBarObservable.onNext(messages["importInvalidAudioFileError"])
                logger.error(
                    "(Drag-Drop) Invalid import file extension. Input files: ${files.first()}"
                )
                false
            }

            else -> true
        }
    }

    fun generateBook(workbookDescriptor: WorkbookDescriptor) {

        val workbook = workbookRepository.get(
            workbookDescriptor.sourceCollection,
            workbookDescriptor.targetCollection
        )
        val chapters = collectionRepository.getChildren(workbookDescriptor.sourceCollection)
            .blockingGet()

        val chapter = chapters.first()
        val chapterVerseContents = contentRepository.getByCollection(chapter).blockingGet()
        val verseText = chapterVerseContents.filter { it.labelKey == "verse" }.map{ it.text }
        println(verseText.size)
    }

    fun generateForChapter(chapter: Chapter) {
        val chunksText = chapter.chunks.blockingGet()
            .map { it.textItem.text }

        val audio = audioGenerator.convertTextToAudio(chunksText)

        // delete/restart chapter


        // import to chapter content

    }
}
