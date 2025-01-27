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
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.Take
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.audio.AudioGenerator
import org.wycliffeassociates.otter.common.domain.audio.MarkerGenerator
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.TakeCreator
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDescriptorRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import java.io.File
import java.time.LocalDate
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

    @Inject
    lateinit var takeCreator: TakeCreator

    @Inject
    lateinit var markerGenerator: MarkerGenerator

    @Inject
    lateinit var takeRepository: ITakeRepository

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

    fun generateBook(workbookDescriptor: WorkbookDescriptor): Completable {

        val workbook = workbookRepository.get(
            workbookDescriptor.sourceCollection,
            workbookDescriptor.targetCollection
        )
        val sourceChapters = collectionRepository.getChildren(workbookDescriptor.sourceCollection)
            .blockingGet()
        val targetChapters = collectionRepository.getChildren(workbookDescriptor.targetCollection)
            .blockingGet()

        return workbook.target.chapters
            .toList() // force emit from deferred observable
            .flattenAsObservable { it }
            .map { ch ->
                val srcChapter = sourceChapters.first { it.sort == ch.sort }
                val targetChapter = targetChapters.first { it.sort == ch.sort }
                val chapterVerseContents = contentRepository.getByCollection(srcChapter).blockingGet()
                val verseText = chapterVerseContents.filter { it.labelKey == "verse" }.map { it.text!! }

                val chapterMetaContent = contentRepository.getCollectionMetaContent(targetChapter).blockingGet()
                generateForChapter(workbook, ch, chapterMetaContent, verseText)
            }
            .subscribeOn(Schedulers.io())
            .ignoreElements()
    }

    private fun generateForChapter(
        workbook: Workbook,
        chapter: Chapter,
        chapterContent: Content,
        chunkTextList: List<String>
    ) {
        println("Generate chapter ${chapter.sort}")
        val generatedAudio = audioGenerator.convertTextToAudio(chunkTextList)
//        markerGenerator.generate(generatedAudio, chunkTextList)

        // delete/restart chapter
//        workbook.projectFilesAccessor.getChapterAudioDir(
//            workbook,
//            chapter
//        )
//            ?.listFiles()
//            ?.forEach { it.deleteRecursively() }

        // import to chapter content
        val namer = getFileNamer(workbook, chapter)
        val takeNumber = 1
        val chapterNumber = namer.formatChapterNumber()
        val chapterAudioDir = workbook.projectFilesAccessor.audioDir
            .resolve(chapterNumber)
            .apply { mkdirs() }

        val chapterFile = chapterAudioDir.resolve(namer.generateName(takeNumber, AudioFileFormat.WAV))
            .also { generatedAudio.copyTo(it, overwrite = true) }

        val take = Take(
            chapterFile.name,
            chapterFile,
            takeNumber,
            LocalDate.now(),
            null,
            false,
            CheckingStatus.UNCHECKED,
            null,
            listOf()
        )
        val insertedId = takeRepository.insertForContent(take, chapterContent).blockingGet()
        take.id = insertedId
        chapterContent.selectedTake = take
        contentRepository.update(chapterContent).blockingAwait()
    }

    private fun getFileNamer(
        workbook: Workbook,
        chapter: Chapter,
    ): FileNamer {
        return WorkbookFileNamerBuilder.createFileNamer(
            workbook = workbook,
            chapter = chapter,
            chunk = null,
            recordable = chapter,
            rcSlug = workbook.sourceMetadataSlug
        )
    }
}
