package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.License
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.AudioExporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.io.zip.IFileWriter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import org.wycliffeassociates.resourcecontainer.entity.MediaManifest
import org.wycliffeassociates.resourcecontainer.entity.MediaProject
import java.io.File
import java.util.*
import javax.inject.Inject

class SourceProjectExporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider
) : ProjectExporter() {
    @Inject
    lateinit var audioExporter: AudioExporter

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val exportMediaTypes = listOf(AudioFileFormat.MP3.extension, "cue")

    override fun export(
        directory: File,
        projectMetadataToExport: ResourceMetadata,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor
    ): Single<ExportResult> {
        val projectSourceMetadata = workbook.source.linkedResources
            .firstOrNull { it.identifier == projectMetadataToExport.identifier }
            ?: workbook.source.resourceMetadata

        val contributors = projectFilesAccessor.getContributorInfo()
        val zipFilename = makeExportFilename(workbook, projectSourceMetadata)
        val zipFile = directory.resolve(zipFilename)

        projectFilesAccessor.initializeResourceContainerInFile(workbook, zipFile)
        setContributorInfo(contributors, zipFile)

        val fileWriter = directoryProvider.newFileWriter(zipFile)

        return exportTakeFiles(workbook, fileWriter, contributors)
            .doOnComplete {
                fileWriter.close() // must close before changing file extension or NoSuchFileException
                updateProjectMetadata(zipFile, workbook.source.resourceMetadata.path, workbook)
                restoreFileExtension(zipFile, OratureFileFormat.ORATURE.extension)
            }
            .doOnError {
                logger.error("Error while exporting project as source.", it)
            }
            .toSingle {
                ExportResult.SUCCESS
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }

    private fun exportTakeFiles(
        workbook: Workbook,
        fileWriter: IFileWriter,
        contributors: List<Contributor>
    ): Completable {
        val tempExportDir = directoryProvider.tempDirectory
            .resolve("export${Date().time}")
            .apply { mkdirs() }

        val license = License.get(workbook.target.resourceMetadata.license)

        return workbook.target.chapters
            .filter { it.audio.selected.value?.value != null }
            .flatMapCompletable { chapter ->
                chapter.audio.selected.value!!.value!!.let { take ->
                    val takeToExport = tempExportDir.resolve(
                        templateAudioFileName(
                            workbook.target.language.slug,
                            workbook.target.resourceMetadata.identifier,
                            workbook.target.slug,
                            chapter.sort.toString(),
                            AudioFileFormat.MP3.extension
                        )
                    )
                    // update markers for newly copied takes
                    val cues = AudioFile(take.file).metadata.getCues()
                    if (cues.isEmpty()) {
                        Completable.complete()
                    } else {
                        audioExporter.exportMp3(take.file, takeToExport, license, contributors)
                            .andThen(
                                Completable.fromAction {
                                    // update audio metadata
                                    AudioFile(takeToExport).apply {
                                        cues.forEach { metadata.addCue(it.location, it.label) }
                                        update()
                                    }
                                }
                                    .subscribeOn(Schedulers.io())
                            )
                    }
                }
            }
            .andThen(
                Completable
                    .fromAction {
                        // include the .cue files associated with takes within the same directory
                        fileWriter.copyDirectory(tempExportDir, RcConstants.SOURCE_MEDIA_DIR)
                    }
                    .subscribeOn(Schedulers.io())
            )
            .doOnError {
                logger.error("Error while copying takes to export file", it)
                fileWriter.close()
            }
    }

    private fun updateProjectMetadata(
        rcFile: File,
        sourceRCFile: File,
        workbook: Workbook
    ) {
        try {
            ResourceContainer.load(rcFile).use { rc ->
                updateManifest(sourceRCFile, rc)
                rc.media = buildMediaManifest(
                    projectSlug = workbook.target.slug,
                    resourceSlug = workbook.target.resourceMetadata.identifier,
                    languageSlug = workbook.target.language.slug
                )

                rc.write()
            }
        } catch (e: Exception) {
            logger.error("Error while updating project manifest.", e)
            throw e
        }
    }

    private fun updateManifest(sourceRCFile: File, targetRc: ResourceContainer) {
        ResourceContainer.load(sourceRCFile).use { sourceRC ->
            val projects = sourceRC.manifest.projects
            targetRc.manifest.projects = projects

            // copy source text
            sourceRC.accessor
                .getInputStreams(".", "usfm").forEach { fileName, inputStream ->
                    inputStream.use { ins ->
                        targetRc.accessor.write(fileName) {
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
                identifier = "mp3",
                version = "",
                url = "",
                quality = listOf(),
                chapterUrl = "${RcConstants.SOURCE_MEDIA_DIR}/${fileName}"
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
        return "${language}_${resource}_${project}_c${chapterLabel}.$extension"
    }
}