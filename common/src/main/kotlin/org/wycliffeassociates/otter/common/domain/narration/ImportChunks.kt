package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.TakeActions
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import java.io.File
import javax.inject.Inject

class ImportChunks @Inject constructor(private val takeActions: TakeActions) {

    fun execute(
        workbook: Workbook,
        chapter: Chapter,
        projectMetadata: ResourceMetadata,
        projectFilesAccessor: ProjectFilesAccessor,
        chunkFiles: Map<String, File>,
        chunks: Array<Chunk>
    ): Completable {
        return Completable.fromCallable {
            val projectSourceMetadata = workbook.source.linkedResources
                .firstOrNull { it.identifier == projectMetadata.identifier }
                ?: workbook.source.resourceMetadata

            chunkFiles
                .mapKeys { findChunk(it.key, chunks) }
                .filterKeys { it != null }
                .map {
                    val chunk = it.key!!
                    val file = it.value

                    val fileNamer = createFileNamer(
                        workbook = workbook,
                        chapter = chapter,
                        chunk = chunk,
                        rcSlug = projectSourceMetadata.identifier
                    )

                    takeActions.import(
                        audio = chunk.audio,
                        projectAudioDir = projectFilesAccessor.audioDir,
                        namer = fileNamer,
                        take = file,
                        takeNumber = 1
                    ).subscribe()
                }
        }
    }

    private fun createFileNamer(
        workbook: Workbook,
        chapter: Chapter,
        chunk: Chunk,
        rcSlug: String
    ): FileNamer {
        return WorkbookFileNamerBuilder.createFileNamer(
            workbook = workbook,
            chapter = chapter,
            chunk = chunk,
            recordable = chunk,
            rcSlug = rcSlug
        )
    }

    private fun findChunk(title: String, chunks: Array<Chunk>): Chunk? {
        return chunks.singleOrNull { it.title == title }
    }
}