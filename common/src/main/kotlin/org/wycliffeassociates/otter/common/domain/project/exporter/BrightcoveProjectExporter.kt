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
package org.wycliffeassociates.otter.common.domain.project.exporter

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.License
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.AudioExporter
import org.wycliffeassociates.otter.common.domain.audio.WAV_TO_MP3_COMPRESSED_RATE
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveCuePoint
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveCuePointBuilder
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveExportEntry
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveExportProject
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveExportReport
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveMetadataBuilder
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveVttBuilder
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveVideoRequest
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerAuthService
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerClient
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerConfigProvider
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerIngestRequest
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerIngestResult
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerTextTrack
import org.wycliffeassociates.otter.common.domain.brightcove.CountryInfo
import org.wycliffeassociates.otter.common.domain.brightcove.CountryInfoResolver
import org.wycliffeassociates.otter.common.domain.brightcove.VerseTimingProvider
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.utils.mapNotNull
import java.io.File
import java.util.Date
import javax.inject.Inject

class BrightcoveProjectExporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val configProvider: BrightcoveWorkerConfigProvider,
    private val workerClient: BrightcoveWorkerClient,
    private val authService: BrightcoveWorkerAuthService,
    private val countryInfoResolver: CountryInfoResolver,
    private val verseTimingProvider: VerseTimingProvider
) : IProjectExporter {

    @Inject
    lateinit var audioExporter: AudioExporter

    private val logger = LoggerFactory.getLogger(javaClass)
    private val metadataBuilder = BrightcoveMetadataBuilder()
    private val cuePointBuilder = BrightcoveCuePointBuilder()
    private val vttBuilder = BrightcoveVttBuilder()
    private val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()

    override fun export(
        outputDirectory: File,
        workbook: Workbook,
        callback: ProjectExporterCallback?,
        options: ExportOptions?
    ): Single<ExportResult> {
        return configProvider.load()
            .flatMap { config ->
                authService.getAccessToken(config).flatMap { token ->
                    Single.fromCallable {
                    val projectAccessor = ProjectFilesAccessor(
                        directoryProvider,
                        workbook.source.resourceMetadata,
                        workbook.target.resourceMetadata,
                        workbook.target.toCollection()
                    )
                    val contributors = try {
                        projectAccessor.getContributorInfo()
                    } catch (e: Exception) {
                        logger.warn("Failed to read contributor info; continuing with empty list", e)
                        emptyList()
                    }
                    val license = License.get(workbook.target.resourceMetadata.license)
                    val countryInfo = resolveCountryInfo(workbook)
                    val projectMode = projectAccessor.getProjectMode()
                    val includeVtt = projectMode == ProjectMode.NARRATION

                    callback?.onNotifyProgress(0.0, messageKey = "exportingTakes")

                    val chapters = workbook.target.chapters
                        .filter { chapterFilter(it, options) }
                        .toList()
                        .blockingGet()

                    val reportEntries = mutableListOf<BrightcoveExportEntry>()
                    var overallSuccess = true
                    val total = chapters.size

                    val tempDir = directoryProvider.tempDirectory
                        .resolve("brightcove-export-${Date().time}")
                        .apply { mkdirs() }

                    chapters.forEachIndexed { index, chapter ->
                        val take = chapter.getSelectedTake()
                        if (take == null) {
                            return@forEachIndexed
                        }

                        val mp3File = tempDir.resolve("chapter-${chapter.sort}.mp3")
                        val vttFile = if (includeVtt) {
                            tempDir.resolve("${mp3File.nameWithoutExtension}.vtt")
                        } else {
                            null
                        }
                        var videoRequest: BrightcoveVideoRequest? = null

                        try {
                            val metadata = AudioExporter.ExportMetadata(license, contributors)
                            audioExporter.exportMp3(take.file, mp3File, metadata).blockingAwait()
                            updateMp3Progress(callback, index + 1, total)

                            val timing = verseTimingProvider.getTiming(take.file)
                            val cuePoints = cuePointBuilder.buildCuePoints(
                                bookCode = workbook.target.slug,
                                chapterNumber = chapter.sort,
                                verseMarkers = timing.markers
                            )
                            videoRequest = buildVideoRequest(workbook, chapter, countryInfo, cuePoints)
                            val uploadId = videoRequest.referenceId

                            if (includeVtt) {
                                val chapterContent = try {
                                    projectAccessor.getChapterContent(
                                        workbook.source.slug,
                                        chapter.sort,
                                        showVerseNumber = false
                                    )
                                } catch (e: Exception) {
                                    logger.warn("Failed to read source text; continuing with empty VTT cues", e)
                                    emptyList()
                                }
                                val vttCues = vttBuilder.buildCues(
                                    bookCode = workbook.target.slug,
                                    chapterNumber = chapter.sort,
                                    verseMarkers = timing.markers,
                                    totalFrames = timing.totalFrames,
                                    chapterContent = chapterContent
                                )
                                vttBuilder.writeVtt(requireNotNull(vttFile), vttCues)
                            }

                            val audioUpload = workerClient
                                .upload(config, token, uploadId, mp3File)
                                .blockingGet()

                            val vttUpload = if (includeVtt) {
                                workerClient.upload(config, token, uploadId, requireNotNull(vttFile)).blockingGet()
                            } else {
                                null
                            }

                            val textTrack = if (includeVtt) {
                                BrightcoveWorkerTextTrack(
                                    srclang = workbook.target.language.slug,
                                    kind = "subtitles",
                                    label = requireNotNull(vttFile).name,
                                    isDefault = true
                                )
                            } else {
                                null
                            }

                            val ingestResult = workerClient
                                .ingest(
                                    config,
                                    token,
                                    BrightcoveWorkerIngestRequest(
                                        uploadId = uploadId,
                                        audioKey = audioUpload.key,
                                        vttKey = vttUpload?.key,
                                        video = requireNotNull(videoRequest),
                                        textTrack = textTrack,
                                        ingestProfile = config.ingestProfile,
                                        callbacks = config.callbacks
                                    )
                                )
                                .blockingGet()

                            reportEntries.add(
                                successEntry(
                                    chapter,
                                    take.name,
                                    requireNotNull(videoRequest),
                                    ingestResult.videoId,
                                    vttFile,
                                    vttUpload?.key,
                                    ingestResult
                                )
                            )
                        } catch (e: Exception) {
                            overallSuccess = false
                            logger.error("Brightcove export failed for chapter ${chapter.sort}", e)
                            val fallbackRequest = videoRequest ?: buildVideoRequest(
                                workbook,
                                chapter,
                                countryInfo,
                                emptyList()
                            )
                            reportEntries.add(
                                failureEntry(chapter, take.name, fallbackRequest, vttFile, e)
                            )
                        } finally {
                            mp3File.delete()
                            vttFile?.delete()
                            updateIngestProgress(callback, index + 1, total)
                        }
                    }

                    val reportFile = writeReport(
                        outputDirectory,
                        workbook,
                        reportEntries
                    )

                    if (overallSuccess) {
                        callback?.onNotifyProgress(100.0)
                        callback?.onNotifySuccess(workbook.target.toCollection(), reportFile)
                        ExportResult.SUCCESS
                    } else {
                        callback?.onNotifyProgress(100.0)
                        ExportResult.FAILURE
                    }
                    }
                }
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
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

    private fun chapterFilter(chapter: Chapter, options: ExportOptions?): Boolean {
        val included = options?.chapters?.contains(chapter.sort) ?: true
        return included && chapter.hasSelectedAudio()
    }

    private fun resolveCountryInfo(workbook: Workbook): CountryInfo {
        return try {
            countryInfoResolver.resolve(workbook.target.language).blockingGet()
        } catch (e: Exception) {
            logger.warn("Country resolver failed; continuing without country info", e)
            CountryInfo(null, null)
        }
    }

    private fun buildVideoRequest(
        workbook: Workbook,
        chapter: Chapter,
        countryInfo: CountryInfo,
        cuePoints: List<BrightcoveCuePoint>
    ): BrightcoveVideoRequest {
        return metadataBuilder.buildVideoRequest(
            languageCode = workbook.target.language.slug,
            resourceType = workbook.target.resourceMetadata.identifier,
            canonicalOrder = workbook.target.sort,
            bookCode = workbook.target.slug,
            localizedBookName = workbook.target.title,
            chapter = chapter.sort,
            countryInfo = countryInfo,
            cuePoints = cuePoints
        )
    }

    private fun updateMp3Progress(callback: ProjectExporterCallback?, index: Int, total: Int) {
        if (total <= 0) return
        val percent = 30.0 * (index.toDouble() / total.toDouble())
        callback?.onNotifyProgress(percent, messageKey = "exportingTakes")
    }

    private fun updateIngestProgress(callback: ProjectExporterCallback?, index: Int, total: Int) {
        if (total <= 0) return
        val percent = 30.0 + (60.0 * (index.toDouble() / total.toDouble()))
        callback?.onNotifyProgress(percent, messageKey = "exportingTakes")
    }

    private fun writeReport(
        outputDirectory: File,
        workbook: Workbook,
        entries: List<BrightcoveExportEntry>
    ): File {
        val reportFile = outputDirectory.resolve("brightcove-export-${Date().time}.json")
        val report = BrightcoveExportReport(
            project = BrightcoveExportProject(
                languageCode = workbook.target.language.slug,
                bookCode = workbook.target.slug,
                canonicalOrder = workbook.target.sort,
                localizedBookName = workbook.target.title
            ),
            entries = entries
        )
        mapper.writeValue(reportFile, report)
        return reportFile
    }

    private fun successEntry(
        chapter: Chapter,
        takeName: String?,
        request: BrightcoveVideoRequest,
        videoId: String?,
        vttFile: File?,
        textTrackUrl: String?,
        ingest: BrightcoveWorkerIngestResult
    ): BrightcoveExportEntry {
        return BrightcoveExportEntry(
            chapter = chapter.sort,
            takeName = takeName,
            videoName = request.name,
            videoId = videoId,
            vttFile = vttFile?.absolutePath,
            textTrackUrl = textTrackUrl,
            ingestJobId = ingest.ingestJobId,
            status = "SUCCESS",
            error = null
        )
    }

    private fun failureEntry(
        chapter: Chapter,
        takeName: String?,
        request: BrightcoveVideoRequest,
        vttFile: File?,
        error: Exception
    ): BrightcoveExportEntry {
        return BrightcoveExportEntry(
            chapter = chapter.sort,
            takeName = takeName,
            videoName = request.name,
            videoId = null,
            vttFile = vttFile?.absolutePath,
            textTrackUrl = null,
            ingestJobId = null,
            status = "FAILURE",
            error = error.message
        )
    }
}
