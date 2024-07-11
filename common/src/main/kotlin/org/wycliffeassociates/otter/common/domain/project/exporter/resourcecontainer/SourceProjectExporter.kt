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
package org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.AudioMetadataFileFormat
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.License
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.AudioExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.ProjectExporterCallback
import org.wycliffeassociates.otter.common.domain.audio.WAV_TO_MP3_COMPRESSED_RATE
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.ScriptureBurritoUtils
import org.wycliffeassociates.otter.common.io.zip.IFileWriter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.utils.mapNotNull
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import org.wycliffeassociates.resourcecontainer.entity.MediaManifest
import org.wycliffeassociates.resourcecontainer.entity.MediaProject
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.readText

class SourceProjectExporter @Inject constructor(
    directoryProvider: IDirectoryProvider,
    val burritoUtils: ScriptureBurritoUtils
) : RCProjectExporter(directoryProvider) {
    @Inject
    lateinit var audioExporter: AudioExporter

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val exportMediaTypes = listOf(
        AudioFileFormat.MP3.extension,
        AudioMetadataFileFormat.CUE.extension
    )

    override fun export(
        outputDirectory: File,
        workbook: Workbook,
        callback: ProjectExporterCallback?,
        options: ExportOptions?
    ): Single<ExportResult> {
        val resourceMetadata = workbook.target.resourceMetadata
        val projectSourceMetadata = workbook.source.linkedResources
            .firstOrNull { it.identifier == resourceMetadata.identifier }
            ?: workbook.source.resourceMetadata

        val projectAccessor = workbook.projectFilesAccessor
        val contributors = projectAccessor.getContributorInfo()
        val zipFilename = makeExportFilename(workbook, projectSourceMetadata)
        val targetZip = outputDirectory.resolve(zipFilename)

        logger.info("Exporting project as source: ${targetZip.nameWithoutExtension}")

        projectAccessor.initializeResourceContainerInFile(workbook, targetZip)
        setContributorInfo(contributors, targetZip)
        callback?.onNotifyProgress(10.0, messageKey = "compilingChapters")

        return compileCompletedChapters(workbook, projectSourceMetadata, projectAccessor)
            .onErrorComplete()
            .doOnComplete { callback?.onNotifyProgress(50.0, messageKey = "exportingTakes") }
            .andThen(
                export(targetZip, workbook, contributors, callback, options)
            )
    }

    override fun estimateExportSize(workbook: Workbook, chapterFilter: List<Int>): Long {
        return workbook.target.chapters
            .filter { it.sort in chapterFilter }
            .mapNotNull { it.getSelectedTake()?.file }
            .reduce(0L) { size, nextFile ->
                when (AudioFileFormat.of(nextFile.extension)) {
                    AudioFileFormat.MP3 -> size + nextFile.length()
                    AudioFileFormat.WAV -> size + nextFile.length() / WAV_TO_MP3_COMPRESSED_RATE
                    else -> size
                }
            }
            .blockingGet()
    }

    private fun export(
        exportFile: File,
        workbook: Workbook,
        contributors: List<Contributor>,
        callback: ProjectExporterCallback?,
        options: ExportOptions?
    ): Single<ExportResult> {
        val fileWriter = directoryProvider.newFileWriter(exportFile)

        return exportSelectedTakes(
            workbook,
            fileWriter,
            contributors,
            options?.chapters
        ).map { takes ->
            callback?.onNotifyProgress(80.0, "finishingUp")

            fileWriter.close() // must close before changing file extension or NoSuchFileException
            buildSourceProjectMetadata(
                exportFile,
                workbook.source.resourceMetadata.path,
                workbook,
                takes
            )
            // change extension app-specific format
            val exportedFile = restoreFileExtension(exportFile, OratureFileFormat.ORATURE.extension)
            callback?.onNotifySuccess(workbook.target.toCollection(), exportedFile)
            ExportResult.SUCCESS
        }

            .doOnError {
                logger.error("Error while exporting project as source.", it)
                fileWriter.close()
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }

    private fun exportSelectedTakes(
        workbook: Workbook,
        fileWriter: IFileWriter,
        contributors: List<Contributor>,
        chapterFilter: List<Int>? = null
    ): Single<Map<Int, List<File>>> {
        val tempExportDir = directoryProvider.tempDirectory
            .resolve("export${Date().time}")
            .apply { mkdirs() }
        val license = License.get(workbook.target.resourceMetadata.license)
        return workbook
            .target
            .chapters
            .filter { chapterFilter?.contains(it.sort) ?: true }
            .map { Pair(it.audio.selected.value?.value, it.sort) }
            .filter { (take, _) -> take != null }
            .map { (take, sort) ->
                val exportPath = tempExportDir.resolve(
                    templateAudioFileName(
                        workbook.target.language.slug,
                        workbook.target.resourceMetadata.identifier,
                        workbook.target.slug,
                        sort.toString(),
                        AudioFileFormat.MP3.extension
                    )
                )
                Pair(take, exportPath)
            }
            .map { (take, exportPath) ->
                exportTake(take!!, exportPath, license, contributors).blockingAwait()
                take
            }
            .collectInto(mutableListOf<Take>()) { takes, take ->
                takes.add(take)
            }
            .map {
                // include the .cue files associated with takes within the same directory
                fileWriter.copyDirectory(tempExportDir, RcConstants.SOURCE_MEDIA_DIR)
                gatherAudioFilesByChapter(tempExportDir)
            }
            .subscribeOn(Schedulers.io())
            .doOnError {
                logger.error("Error while copying takes to export file", it)
            }
    }

    private fun gatherAudioFilesByChapter(dir: File): Map<Int, List<File>> {
        val map = mutableMapOf<Int, List<File>>()
        dir.listFiles()?.forEach { audioFile ->
            val chapterPattern =  Regex("""_c(\d+)""")

            val chapterNumber = chapterPattern.find(audioFile.name)!!.groupValues[1].toInt()

            if (map.containsKey(chapterNumber)) {
                (map[chapterNumber]!! as MutableList).add(audioFile)
            } else {
                map[chapterNumber] = mutableListOf(audioFile)
            }
        }
        return map
    }

    private fun exportTake(
        take: Take,
        exportPath: File,
        license: License?,
        contributors: List<Contributor>
    ): Completable {
        // update markers for newly copied takes
        val cues = OratureAudioFile(take.file).getCues()
        return if (cues.isEmpty()) {
            Completable.complete()
        } else {
            val metadata = AudioExporter.ExportMetadata(
                license, contributors, cues
            )
            audioExporter
                .exportMp3(take.file, exportPath, metadata)
                .subscribeOn(Schedulers.io())
        }
    }

    private fun buildSourceProjectMetadata(
        exportFile: File,
        sourceRCFile: File,
        workbook: Workbook,
        takes: Map<Int, List<File>>
    ) {
        try {
            ResourceContainer.load(exportFile).use { rc ->
                updateManifest(sourceRCFile, rc)
                rc.media = buildMediaManifest(
                    projectSlug = workbook.target.slug,
                    resourceSlug = workbook.target.resourceMetadata.identifier,
                    languageSlug = workbook.target.language.slug
                )

                rc.write()

                val tempFile = Files.createTempFile(
                    directoryProvider.tempDirectory.toPath(),
                    "out_burrito",
                    "json"
                )
                burritoUtils.writeBurritoManifest(
                    workbook,
                    takes,
                    rc,
                    tempFile.outputStream()
                )
                rc.accessor.write("metadata.json") {
                    tempFile.inputStream().transferTo(it)
                }
                tempFile.deleteIfExists()
            }
        } catch (e: Exception) {
            logger.error("Error while updating project manifest.", e)
            throw e
        }
    }

    private fun updateManifest(sourceRCFile: File, exportRC: ResourceContainer) {
        ResourceContainer.load(sourceRCFile).use { sourceRC ->
            val projects = sourceRC.manifest.projects
            exportRC.manifest.projects = projects

            // copy source text
            sourceRC
                .accessor
                .getInputStreams(".", "usfm")
                .forEach { (fileName, inputStream) ->
                    inputStream.use { ins ->
                        exportRC.accessor.write(fileName) {
                            it.buffered().use { out ->
                                out.write(ins.buffered().readBytes())
                            }
                        }
                    }
                }
        }
    }

    private fun buildMediaManifest(
        projectSlug: String,
        languageSlug: String,
        resourceSlug: String
    ): MediaManifest {
        val mediaManifest = let {
            MediaManifest().apply {
                projects = listOf(MediaProject(identifier = projectSlug))
            }
        }
        val mediaList = exportMediaTypes.map { mediaType ->
            val fileName = templateAudioFileName(
                languageSlug, resourceSlug, projectSlug, "{chapter}", mediaType
            )
            Media(
                identifier = mediaType,
                version = "",
                url = "",
                quality = listOf(),
                chapterUrl = "${RcConstants.SOURCE_MEDIA_DIR}/$fileName"
            )
        }

        mediaManifest.projects.first().media = mediaList

        return mediaManifest
    }

    private fun templateAudioFileName(
        language: String,
        resource: String,
        project: String,
        chapterLabel: String,
        extension: String
    ): String {
        return "${language}_${resource}_${project}_c$chapterLabel.$extension"
    }
}