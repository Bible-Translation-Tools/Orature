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
package org.wycliffeassociates.otter.common.domain.narration

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import java.io.File
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class Narration @AssistedInject constructor(
    private val splitAudioOnCues: SplitAudioOnCues,
    private val audioFileUtils: AudioFileUtils,
    private val recorder: IAudioRecorder,
    private val player: IAudioPlayer,
    @Assisted private val workbook: Workbook,
    @Assisted private val chapter: Chapter
) {

    private val DEFAULT_FRAME_SIZE_BYTES = 2
    private val logger = LoggerFactory.getLogger(Narration::class.java)

    private val history = NarrationHistory()
    private var chapterRepresentation = ChapterRepresentation(workbook, chapter)
    private val chapterReaderConnection =
        chapterRepresentation.getAudioFileReader() as ChapterRepresentation.ChapterRepresentationConnection

    private var audioLoaded = false

    private val isRecording = AtomicBoolean(false)
    private val uncommittedRecordedFrames = AtomicInteger(0)

    private val disposables = CompositeDisposable()

    val audioReader: AudioFileReader
        get() = chapterRepresentation.getAudioFileReader()

    val totalVerses: List<AudioMarker>
        get() {
            val verses = chapterRepresentation
                .totalVerses
                .map {
                    it.marker
                }
            return verses
        }

    val activeVerses: List<AudioMarker>
        get() {
            val verses = chapterRepresentation
                .activeVerses
                .map {
                    it.copyMarker(
                        location = chapterRepresentation.audioLocationToLocationInChapter(it.firstFrame())
                    )
                }
            return verses
        }

    fun versesWithRecordings(): List<Boolean> {
        return chapterRepresentation.versesWithRecordings()
    }

    val onActiveVersesUpdated: PublishSubject<List<AudioMarker>>
        get() = chapterRepresentation.onActiveVersesUpdated

    private val firstVerse: AudioMarker

    private var writer: WavFileWriter? = null

    private var lockedVerseIndex: Int? = null

    private var takeToModify: Take?

    init {
        val writer = initializeWavWriter()

        firstVerse = getFirstVerseMarker()
        restoreFromExistingChapterAudio()
        chapterRepresentation.loadFromSerializedVerses()
        recorder.start()
        disposables.addAll(
            activeRecordingFrameCounter(writer),
            resetUncommittedFramesOnUpdatedVerses(),
        )
        loadChapterIntoPlayer()
        takeToModify = chapter.getSelectedTake()

    }

    fun lockToVerse(verseIndex: Int?) {
        lockedVerseIndex = verseIndex
        chapterReaderConnection.lockToVerse(verseIndex)
    }

    /**
     * Counts the number of audio frames that have been recorded since activating a recording
     */
    private fun activeRecordingFrameCounter(writer: WavFileWriter): Disposable {
        return Observable
            .combineLatest(writer.isWriting, getRecorderAudioStream())
            { isWriting, bytes -> Pair(isWriting, bytes) }
            .map { (isWriting, bytes) ->
                if (isWriting) {
                    uncommittedRecordedFrames.addAndGet(bytes.size / DEFAULT_FRAME_SIZE_BYTES)
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
            .also { disposables.add(it) }
    }

    /**
     * Resets the count of recorded frames when the writer pauses and thus the active verse list gets updated
     */
    private fun resetUncommittedFramesOnUpdatedVerses(): Disposable {
        return onActiveVersesUpdated.subscribe {
            uncommittedRecordedFrames.set(0)
        }
    }

    private fun getFirstVerseMarker(): AudioMarker {
        return chapterRepresentation.totalVerses.first().marker
    }

    fun loadFromSelectedChapterFile() {
        restoreFromExistingChapterAudio(true)
    }

    fun getPlayer(): IAudioPlayer {
        return player
    }

    fun getRecorderAudioStream(): Observable<ByteArray> {
        return recorder.getAudioStream()
    }

    fun closeRecorder() {
        isRecording.set(false)
        recorder.stop()

        writer?.writer?.dispose()
        writer = null
    }

    fun closeChapterRepresentation() {
        chapterRepresentation.closeConnections()
    }

    fun undo() {
        // Ensures we are not locked to a verse and that the location is in the relative chapter space
        seek(getLocationInChapter(), true)
        history.undo(chapterRepresentation.totalVerses)
        chapterRepresentation.onVersesUpdated()

        NarrationTakeModifier.modifyAudioData(
            takeToModify,
            chapterRepresentation.getAudioFileReader(),
            activeVerses
        )
    }

    fun redo() {
        // Ensures we are not locked to a verse and that the location is in the relative chapter space
        seek(getLocationInChapter(), true)
        history.redo(chapterRepresentation.totalVerses)
        chapterRepresentation.onVersesUpdated()

        NarrationTakeModifier.modifyAudioData(
            takeToModify,
            chapterRepresentation.getAudioFileReader(),
            activeVerses
        )
    }

    fun finalizeVerse(verseIndex: Int) {
        val loc = chapterRepresentation.finalizeVerse(verseIndex, history)
        val relLoc = chapterRepresentation.audioLocationToLocationInChapter(loc)

        audioLoaded = false
        loadChapterIntoPlayer()
        seek(relLoc)
    }

    fun onNewVerse(verseIndex: Int) {
        val action = NewVerseAction(verseIndex)
        execute(action)

        audioLoaded = false
        loadChapterIntoPlayer()

        player.seek(player.getDurationInFrames())
        writer?.start()
        isRecording.set(true)
    }

    fun onRecordAgain(verseIndex: Int) {
        val action = RecordAgainAction(verseIndex)
        execute(action)

        audioLoaded = false
        loadChapterIntoPlayer()

        seek(activeVerses[verseIndex].location)
        writer?.start()
        isRecording.set(true)
    }

    fun onSaveRecording(verseIndex: Int) {
        val loc = chapterRepresentation.finalizeVerse(verseIndex, history)
        val relLoc = chapterRepresentation.audioLocationToLocationInChapter(loc)

        audioLoaded = false
        loadChapterIntoPlayer()

        seek(relLoc)

        writer?.pause()
        uncommittedRecordedFrames.set(0)
        isRecording.set(false)

        NarrationTakeModifier.modifyAudioData(
            takeToModify,
            chapterRepresentation.getAudioFileReader(),
            activeVerses
        )
    }

    fun onVerseMarkerMoved(verseIndex: Int, delta: Int) {
        val action = MoveMarkerAction(verseIndex, delta)
        execute(action)

        NarrationTakeModifier.modifyMetadata(takeToModify, activeVerses)
    }

    fun onEditVerse(verseIndex: Int, editedFile: File) {

        loadSectionIntoPlayer(activeVerses[verseIndex])

        val scratchAudio = chapterRepresentation.scratchAudio
        val start = if (scratchAudio.totalFrames == 0) 0 else scratchAudio.totalFrames + 1
        audioFileUtils.appendFile(chapterRepresentation.scratchAudio, editedFile)
        val end = chapterRepresentation.scratchAudio.totalFrames

        val action = EditVerseAction(verseIndex, start, end)
        execute(action)

        NarrationTakeModifier.modifyAudioData(
            takeToModify,
            chapterRepresentation.getAudioFileReader(),
            activeVerses
        )

        audioLoaded = false
        loadChapterIntoPlayer()
    }

    fun onResetAll() {
        val action = ResetAllAction(chapter.audio)
        execute(action)
    }

    private fun onChapterEdited(newVerses: List<VerseNode>) {
        val action = ChapterEditedAction(newVerses)
        execute(action)

        val hasAllVersesRecorded = newVerses.any { !it.placed }.not()

        if (hasAllVersesRecorded) {
            NarrationTakeModifier.modifyAudioData(
                takeToModify,
                chapterRepresentation.getAudioFileReader(),
                activeVerses
            )
        }
    }

    fun pauseRecording() {
        writer?.pause()
        isRecording.set(false)
    }

    fun resumeRecording() {

        // Ensures that the entire chapter is loaded into the player
        lockToVerse(null)
        audioLoaded = false
        loadChapterIntoPlayer()

        seek(player.getDurationInFrames())
        writer?.start()
        isRecording.set(true)
    }

    fun resumeRecordingAgain() {
        // Seeks to the end of the scratchAudio, since the re-record has not yet been finalized.
        val lastRecordingPosition = chapterRepresentation.scratchAudio.totalFrames
        player.seek(chapterRepresentation.audioLocationToLocationInChapter(lastRecordingPosition))
        writer?.start()
        isRecording.set(true)
    }

    fun hasUndo(): Boolean {
        return history.hasUndo()
    }

    fun hasRedo(): Boolean {
        return history.hasRedo()
    }

    fun getSectionAsFile(index: Int): File {
        lockToVerse(index)
        chapterReaderConnection.seek(0)
        return audioFileUtils.getSectionAsFile(
            chapterRepresentation.scratchAudio,
            chapterReaderConnection
        )
    }

    fun loadSectionIntoPlayer(verse: AudioMarker) {
        if (!audioLoaded) {
            player.load(chapterReaderConnection)
            audioLoaded = true
        }
        logger.info("Loading ${verse.formattedLabel} into player")
        val range: IntRange? = chapterRepresentation.getRangeOfMarker(verse)
        logger.info("Playback range is ${range?.start}-${range?.last}")
        range?.let {
            val wasPlaying = player.isPlaying()
            player.pause()
            lockToVerse(activeVerses.indexOf(verse))
            chapterReaderConnection.start = range.first
            chapterReaderConnection.end = range.last
            seek(0)
            if (wasPlaying) player.play()
        }
    }

    fun loadChapterIntoPlayer() {
        if (!audioLoaded) {
            player.load(chapterReaderConnection)
            audioLoaded = true
        }

        val wasPlaying = player.isPlaying()
        player.pause()
        lockToVerse(null)
        chapterReaderConnection.start = null
        chapterReaderConnection.end = null

        if (player.getLocationInFrames() == player.getDurationInFrames()) {
            player.seek(0)
            chapterReaderConnection.seek(0)
        }

        if (wasPlaying) player.play()
    }

    private fun initializeWavWriter(): WavFileWriter {
        writer = WavFileWriter(
            chapterRepresentation.scratchAudio,
            recorder.getAudioStream(),
            true
        ) {
            /* no op */
        }
        return writer!!
    }

    private fun execute(action: NarrationAction) {
        if (!audioLoaded) {
            player.load(chapterReaderConnection)
            audioLoaded = true
        }
        // Ensures we are not locked to a verse and that the location is in the relative chapter space
        seek(getLocationInChapter(), true)
        history.execute(action, chapterRepresentation.totalVerses, chapterRepresentation.scratchAudio)
        chapterRepresentation.onVersesUpdated()
    }

    private fun restoreFromExistingChapterAudio(
        forceUpdate: Boolean = false
    ) {
        val chapterFile = chapter.getSelectedTake()?.file
        val chapterFileExists = chapterFile?.exists() ?: false

        val narrationEmpty = chapterRepresentation.scratchAudio.totalFrames == 0
        val narrationFromChapter = chapterFileExists && narrationEmpty

        if (narrationFromChapter || forceUpdate) {
            val segments = splitAudioOnCues.execute(chapterFile!!, firstVerse)
            val verseNodes = createVersesFromVerseSegments(segments)
            onChapterEdited(verseNodes)
            appendVerseSegmentsToScratchAudio(segments)
        }
    }

    private fun appendVerseSegmentsToScratchAudio(segments: VerseSegments) {
        segments.forEach {
            audioFileUtils.appendFile(chapterRepresentation.scratchAudio, it.value)
        }
    }

    private fun createVersesFromVerseSegments(segments: VerseSegments): List<VerseNode> {
        val nodes = mutableListOf<VerseNode>()
        var start = chapterRepresentation.scratchAudio.totalFrames
        var end = chapterRepresentation.scratchAudio.totalFrames

        val segmentLabels = segments.keys.map { it.formattedLabel }
        totalVerses
            .filter { marker -> marker.formattedLabel !in segmentLabels }
            .forEach {
                nodes.add(
                    VerseNode(
                        false,
                        it,
                        mutableListOf()
                    )
                )
            }

        segments.forEach { (marker, file) ->
            val verseAudio = AudioFile(file)
            end += verseAudio.totalFrames
            val node = VerseNode(
                true,
                marker,
                mutableListOf(IntRange(start, end))
            )
            nodes.add(node)
            start = end + 1
        }

        return nodes.sortedBy { it.marker.sort }
    }


    /**
     * Creates a Single that emits a take where the emission of the take is delayed until the take audio is finished
     * bouncing.
     */
    fun createChapterTakeWithAudio(): Single<Take> {
        return chapter.audio.getNewTakeNumber()
            .flatMap { takeNumber ->
                val namer = WorkbookFileNamerBuilder.createFileNamer(
                    workbook,
                    chapter,
                    null,
                    chapter,
                    workbook.sourceMetadataSlug
                )

                val takeName = namer.generateName(takeNumber, AudioFileFormat.WAV)

                val parentDir = workbook.projectFilesAccessor.getChapterAudioDir(workbook, chapter)
                val takeFile = File(parentDir, takeName)
                val take = Take(takeFile.name, takeFile, takeNumber, MimeType.WAV, LocalDate.now())

                chapter.audio.insertTake(take)

                Single.just(take)
            }
            .flatMap { take ->
                takeToModify = take

                NarrationTakeModifier.modifyAudioDataTask(
                    take,
                    chapterRepresentation.getAudioFileReader(),
                    activeVerses
                )
                    .andThen(Single.just(take))
            }
    }

    /**
     * Creates a Single that emits a take where the emission of the take is not delayed while the take audio is
     * bouncing.
     */
    fun createChapterTake(): Single<Take> {

        return chapter
            .audio
            .getNewTakeNumber()
            .map { takeNumber ->
                val namer =
                    WorkbookFileNamerBuilder.createFileNamer(
                        workbook,
                        chapter,
                        null,
                        chapter,
                        workbook.sourceMetadataSlug
                    )
                Pair(namer.generateName(takeNumber, AudioFileFormat.WAV), takeNumber)
            }
            .map { (takeName, takeNumber) ->
                val parentDir = workbook.projectFilesAccessor.getChapterAudioDir(workbook, chapter)
                val takeFile = File(parentDir, takeName)
                val take = Take(takeFile.name, takeFile, takeNumber, MimeType.WAV, LocalDate.now())
                chapter.audio.insertTake(take)
                take
            }
            .map { take ->
                take
            }
            .doAfterSuccess {
                takeToModify = it

                NarrationTakeModifier.modifyAudioData(
                    takeToModify,
                    chapterRepresentation.getAudioFileReader(),
                    activeVerses
                )
            }
    }

    fun deleteChapterTake(deleteAudioFile: Boolean = false) {

        if (deleteAudioFile) {
            chapter.audio.getSelectedTake()?.file?.delete()
        }

        logger.info("Deleting chapter take")
        chapter
            .audio
            .getSelectedTake()
            ?.deletedTimestamp
            ?.accept(DateHolder.now())
        takeToModify = null
    }

    fun scrollAudio(delta: Int) {
        chapterReaderConnection.seek(delta)
    }

    /**
     * Seeks the player and chapterReaderConnection to a relative chapter location or relative verse location, and
     * allows the caller to specify when to unlock from all verses before seeking to the specified location.
     */
    fun seek(location: Int, unlockFromVerse: Boolean = false) {
        if (unlockFromVerse) {
            lockToVerse(null)
        }
        player.seek(location)
        chapterReaderConnection.seek(location)
    }

    /**
     * Gets the duration of the relative chapter space in frames
     */
    fun getDurationInFrames(): Int {
        return chapterRepresentation.totalFrames
    }

    fun getTotalFrames(): Int {
        return chapterReaderConnection.totalFrames + uncommittedRecordedFrames.get()
    }

    private fun getLocationInChapter(): Int {
        return if (lockedVerseIndex != null) {
            chapterReaderConnection
                .locationInVerseToLocationInChapter(player.getLocationInFrames(), lockedVerseIndex!!)
        } else {
            player.getLocationInFrames()
        }
    }

    fun getLocationInFrames(): Int {
        val chapterLocation = getLocationInChapter()
        return chapterLocation + uncommittedRecordedFrames.get()
    }

    fun close() {
        disposables.dispose()
        player.stop()
        player.close()
        recorder.stop()
        chapterRepresentation.closeConnections()

        if (history.hasRedo() || history.hasUndo()) {
            chapterRepresentation.trim()
        }
        history.clear()
    }

    fun seekToPrevious() {
        player.pause()
        val loc = getLocationInChapter()
        lockToVerse(null)
        val seekLoc = activeVerses.lastOrNull() { it.location < loc }
        seekLoc?.let {
            logger.info("Seeking to previous: ${it.formattedLabel}")
            seek(it.location)
        } ?: run {
            logger.info("Previous marker not found, seeking to 0")
            seek(0)
        }
    }

    fun seekToNext() {
        player.pause()
        val loc = getLocationInChapter()
        lockToVerse(null)

        activeVerses
            .firstOrNull { it.location > loc }
            ?.let {
                logger.info("Seeking to next: ${it.formattedLabel}")
                seek(it.location)
            }
            ?: chapterRepresentation.apply {
                if (activeVerses.isNotEmpty()) {
                    logger.info("Next marker not found, seeking to end of audio")
                    val lastFrame = audioLocationToLocationInChapter(activeVerses.last().lastFrame())
                    seek(lastFrame)
                }
            }
    }

    fun findMarkerAtPosition(position: Int): AudioMarker? {
        val frame = chapterRepresentation.relativeChapterToAbsolute(position)
        return chapterRepresentation.findVerse(frame)?.marker
    }
}

@AssistedFactory
interface NarrationFactory {
    fun create(
        workbook: Workbook,
        chapter: Chapter
    ): Narration
}
