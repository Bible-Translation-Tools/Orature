package org.wycliffeassociates.otter.jvm.workbookapp.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.observableListOf
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class NarrationHistory @Inject constructor(private val directoryProvider: IDirectoryProvider) {

    private val undoHistory = SimpleListProperty<Snapshot>(observableListOf())
    private val redoHistory = SimpleListProperty<Snapshot>(observableListOf())

    private lateinit var workbookDataStore: WorkbookDataStore

    val hasUndoProperty = SimpleBooleanProperty()
    val hasRedoProperty = SimpleBooleanProperty()

    class Snapshot(
        var chapter: File? = null,
        val chunks: Map<Int, File?> = mapOf(),
    )

    init {
        hasUndoProperty.bind(undoHistory.emptyProperty().not())
        hasRedoProperty.bind(redoHistory.emptyProperty().not())
    }

    fun setWorkbookDataStore(workbookDataStore: WorkbookDataStore) {
        this.workbookDataStore = workbookDataStore
    }

    fun snapshot() {
        val chapter = workbookDataStore.chapter
        val chapterTake = chapter.audio.lastTake()
        var chapterTemp: File? = null
        chapterTake?.file?.let { file ->
            directoryProvider.createTempFile(
                "chapter",
                ".${file.extension}"
            ).also { temp ->
                chapterTemp = temp
                file.copyTo(temp, true)
            }
        }

        val chunks = chapter.chunks.value
            ?.associate { chunk ->
                val chunkTake = chunk.audio.lastTake()
                chunkTake?.file?.let { file ->
                    val chunkTemp = directoryProvider.createTempFile(
                        "chunk",
                        ".${file.extension}"
                    )
                    file.copyTo(chunkTemp, true)
                    chunk.sort to chunkTemp
                } ?: (chunk.sort to null)
            } ?: mapOf()

        val snapshot = Snapshot(
            chapterTemp,
            chunks
        )
        undoHistory.add(snapshot)
        redoHistory.clear()
    }

    fun undo() {
        val lastSnapshot = undoHistory.lastOrNull()
        lastSnapshot?.let { snapshot ->
            val redoSnapshot = revert(snapshot)

            undoHistory.remove(snapshot)
            redoHistory.add(redoSnapshot)
        }
    }

    fun redo() {
        val lastSnapshot = redoHistory.lastOrNull()
        lastSnapshot?.let { snapshot ->
            val undoSnapshot = revert(snapshot)

            redoHistory.remove(snapshot)
            undoHistory.add(undoSnapshot)
        }
    }

    fun clearLastSnapshot() {
        if (undoHistory.isNotEmpty()) {
            undoHistory.removeLast()
        }
    }

    private fun revert(snapshot: Snapshot): Snapshot {
        val workbook = workbookDataStore.workbook
        val chapter = workbookDataStore.chapter

        val chapterTake = chapter.audio.lastTake()
        val chapterCached = snapshot.chapter
        var chapterTemp: File? = null
        val chunksTemp = mutableMapOf<Int, File?>()

        chapterTake?.file?.let { file ->
            directoryProvider.createTempFile(
                "chapter",
                ".${file.extension}"
            ).also {
                chapterTemp = it
                file.copyTo(it, true)
            }
        }

        chapterTake?.deletedTimestamp?.accept(DateHolder.now())

        chapterCached?.let { file ->
            val takeNumber = chapter.audio.getNewTakeNumber().blockingGet()
            val newChapterFile = generateTakeFile(
                workbook,
                chapter,
                null,
                chapter,
                AudioFileFormat.WAV,
                takeNumber
            )
            file.copyTo(newChapterFile, true)

            val take = Take(
                newChapterFile.name,
                newChapterFile,
                takeNumber,
                MimeType.WAV,
                createdTimestamp = LocalDate.now()
            )
            chapter.audio.insertTake(take)
        }

        val chunks = chapter.chunks.value ?: listOf()
        chunks.forEach { chunk ->
            val chunkTake = chunk.audio.lastTake()
            val chunkCached = snapshot.chunks[chunk.sort]
            var chunkTemp: File? = null

            chunkTake?.file?.let { file ->
                directoryProvider.createTempFile(
                    "chunk",
                    ".${file.extension}"
                ).also {
                    chunkTemp = it
                    file.copyTo(it, true)
                }
            }

            chunkTake?.deletedTimestamp?.accept(DateHolder.now())

            chunkCached?.let { file ->
                val takeNumber = chunk.audio.getNewTakeNumber().blockingGet()
                val newChunkFile = generateTakeFile(
                    workbook,
                    chapter,
                    chunk,
                    chunk,
                    AudioFileFormat.PCM,
                    takeNumber
                )
                file.copyTo(newChunkFile, true)

                val take = Take(
                    newChunkFile.name,
                    newChunkFile,
                    takeNumber,
                    MimeType.WAV,
                    createdTimestamp = LocalDate.now()
                )
                chunk.audio.insertTake(take)
            }

            chunkTemp?.let {
                chunksTemp[chunk.sort] = it
            }
        }

        return Snapshot(
            chapterTemp,
            chunksTemp
        )
    }

    fun clear() {
        redoHistory.clear()
        undoHistory.clear()
    }

    private fun generateTakeFile(
        workbook: Workbook,
        chapter: Chapter,
        chunk: Chunk?,
        recordable: Recordable,
        format: AudioFileFormat,
        takeNumber: Int
    ): File {
        val namer = WorkbookFileNamerBuilder.createFileNamer(
            workbook = workbook,
            chapter = chapter,
            chunk = chunk,
            recordable = recordable,
            rcSlug = workbook.source.resourceMetadata.identifier
        )
        val fileName = namer.generateName(takeNumber, format)
        val chapterNum = namer.formatChapterNumber()
        val chapterAudioDir = workbook.projectFilesAccessor.audioDir.resolve(chapterNum)

        return chapterAudioDir.resolve(fileName)
    }

    private fun AssociatedAudio.lastTake(): Take? {
        return getAllTakes().lastOrNull {
            it.deletedTimestamp.value?.value == null
        }
    }
}