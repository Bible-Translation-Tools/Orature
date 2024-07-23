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
import io.reactivex.Completable
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
import org.wycliffeassociates.otter.common.domain.audio.AudioBouncer
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import java.io.File
import java.lang.Integer.max
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class Narration @AssistedInject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val splitAudioOnCues: SplitAudioOnCues,
    private val audioFileUtils: AudioFileUtils,
    private val audioBouncer: AudioBouncer,
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
        get() = chapterRepresentation.getActiveMarkers()

    fun versesWithRecordings(): List<Boolean> {
        return chapterRepresentation.versesWithRecordings()
    }

    val onActiveVersesUpdated: PublishSubject<List<AudioMarker>>
        get() = chapterRepresentation.onActiveVersesUpdated

    private val firstVerse: AudioMarker

    private var writer: WavFileWriter? = null

    private var lockedVerseIndex: Int? = null

    private var takeToModify: Take? = null

    init {
        firstVerse = getFirstVerseMarker()
    }

    fun initialize(): Completable {
        return restoreFromExistingChapterAudio()
            .andThen {
                chapterRepresentation.loadFromSerializedVerses()
                disposables.add(resetUncommittedFramesOnUpdatedVerses())
                loadChapterIntoPlayer()
                takeToModify = chapter.getSelectedTake()
                it.onComplete()
            }
    }

    fun startMicrophone() {
        val writer = initializeWavWriter()
        recorder.start()
        disposables.addAll(activeRecordingFrameCounter(writer))
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

    fun loadFromSelectedChapterFile(): Completable {
        return restoreFromExistingChapterAudio(true)
    }


    fun importChapterAudioFile(chapterAudioFile: File): Completable {

        return Completable.fromAction {

            if (!chapterAudioFile.exists()) {
                logger.error("Tried to import a chapter file that does not exists.")
                return@fromAction
            }

            var newSegments = splitAudioOnCues.execute(chapterAudioFile, firstVerse)

            // Removes marker with duplicate label
            newSegments = newSegments
                .entries
                .distinctBy { it.key.formattedLabel }
                .associate { it.toPair() }

            // Only uses markers that correspond to the current chapter
            val totalVerseLabels = totalVerses.map { it.formattedLabel }
            newSegments = newSegments.filterKeys { it.formattedLabel in totalVerseLabels }


            val verseNodes = createVersesFromVerseSegments(newSegments)
            appendVerseSegmentsToScratchAudio(newSegments)
            onChapterAudioImported(verseNodes)
        }
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
        seek(getFrameInChapter(), true)
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
        seek(getFrameInChapter(), true)
        history.redo(chapterRepresentation.totalVerses)
        chapterRepresentation.onVersesUpdated()

        NarrationTakeModifier.modifyAudioData(
            takeToModify,
            chapterRepresentation.getAudioFileReader(),
            activeVerses
        )
    }

    fun finalizeVerse(verseIndex: Int) {
        val absoluteFrame =
            chapterRepresentation.finalizeVerse(verseIndex, history) / chapterRepresentation.frameSizeInBytes
        val relLoc = chapterRepresentation.absoluteFrameToRelativeChapterFrame(absoluteFrame)

        audioLoaded = false
        loadChapterIntoPlayer()
        seek(relLoc)
    }

    fun onNewVerse(verseIndex: Int) {
        val action = NewVerseAction(verseIndex, chapterRepresentation.frameSizeInBytes)
        execute(action)

        audioLoaded = false
        loadChapterIntoPlayer()

        seek(totalVerses[verseIndex].location)

        writer?.start()
        isRecording.set(true)
    }

    fun onRecordAgain(verseIndex: Int) {
        val action = RecordAgainAction(verseIndex, chapterRepresentation.frameSizeInBytes)
        execute(action)

        audioLoaded = false
        loadChapterIntoPlayer()

        seek(totalVerses[verseIndex].location)
        writer?.start()
        isRecording.set(true)
    }

    fun onSaveRecording(verseIndex: Int) {
        val absoluteFrame =
            chapterRepresentation.finalizeVerse(verseIndex, history) / chapterRepresentation.frameSizeInBytes
        val relLoc = chapterRepresentation.absoluteFrameToRelativeChapterFrame(absoluteFrame)

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

    fun onVerseMarkerMoved(verseIndex: Int, deltaFrames: Int) {
        val deltaIndexes = deltaFrames * chapterRepresentation.frameSizeInBytes
        val action = MoveMarkerAction(verseIndex, deltaIndexes)
        execute(action)

        NarrationTakeModifier.modifyMetadata(takeToModify, activeVerses)
    }

    fun onEditVerse(verseIndex: Int, editedFile: File): Completable {

        return Completable.fromAction {
            val scratchAudio = chapterRepresentation.scratchAudio
            val start = if (scratchAudio.totalFrames == 0) 0 else scratchAudio.totalFrames + 1
            audioFileUtils.appendFile(chapterRepresentation.scratchAudio, editedFile)
            val end = chapterRepresentation.scratchAudio.totalFrames

            /* When a new verse recorded with an EXTERNAL plugin comes back empty,
            {start} could be greater than {end} by 1, which is invalid and may cause a crash.
            */
            if (start < end) {
                val frameSize = chapterRepresentation.frameSizeInBytes
                val action = EditVerseAction(verseIndex, start * frameSize, end * frameSize)
                execute(action)
            }

            NarrationTakeModifier.modifyAudioData(
                takeToModify,
                chapterRepresentation.getAudioFileReader(),
                activeVerses
            )

            audioLoaded = false
            loadSectionIntoPlayer(totalVerses[verseIndex])
        }
    }

    fun onResetAll() {
        val action = ResetAllAction(chapter.audio)
        execute(action)
    }

    fun onPlaybackFinished() {
        if (lockedVerseIndex != null) {
            player.pause()
            seek(0)
        }
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

    private fun onChapterAudioImported(newVerses: List<VerseNode>) {
        val action = ChapterImportedAction(newVerses)
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

    fun resumeRecording(index: Int) {
        // Ensures that the entire chapter is loaded into the player
        lockToVerse(null)
        audioLoaded = false
        loadChapterIntoPlayer()

        val lastAbsoluteFrame =
            chapterRepresentation.totalVerses[index].lastIndex() / chapterRepresentation.frameSizeInBytes
        val seekFrame = chapterRepresentation.absoluteFrameToRelativeChapterFrame(lastAbsoluteFrame)
        seek(seekFrame)

        writer?.start()
        isRecording.set(true)
    }

    fun resumeRecordingAgain() {
        // Seeks to the end of the scratchAudio, since the re-record has not yet been finalized.
        val lastRecordingPosition = chapterRepresentation.scratchAudio.totalFrames - 1
        player.seek(chapterRepresentation.absoluteFrameToRelativeChapterFrame(lastRecordingPosition))
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
        return if (totalVerses[index] !in activeVerses) {
            // create new file for unrecorded item
            chapterReaderConnection.seek(0)
            File.createTempFile(
                "tempAudio",
                ".pcm",
                directoryProvider.tempDirectory
            )
        } else {
            lockToVerse(index)
            chapterReaderConnection.seek(0)
            audioFileUtils.getSectionAsFile(
                chapterRepresentation.scratchAudio,
                chapterReaderConnection
            )
        }
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
        seek(getFrameInChapter(), true)
        history.execute(action, chapterRepresentation.totalVerses, chapterRepresentation.scratchAudio)
        chapterRepresentation.onVersesUpdated()
    }

    private fun restoreFromExistingChapterAudio(forceUpdate: Boolean = false): Completable {
        return Completable
            .fromAction {
                val chapterFile = chapter.getSelectedTake()?.file
                val chapterFileExists = chapterFile?.exists() ?: false

                val narrationEmpty = chapterRepresentation.scratchAudio.totalFrames == 0
                val narrationFromChapter = chapterFileExists && narrationEmpty

                if (narrationFromChapter || forceUpdate) {
                    val segments = splitAudioOnCues.execute(chapterFile!!, firstVerse)
                    val verseNodes = createVersesFromVerseSegments(segments)
                    appendVerseSegmentsToScratchAudio(segments)
                    onChapterEdited(verseNodes)
                    if (!forceUpdate) {
                        history.clear()
                    }
                }
            }
            .doOnError { logger.error("Error while restoring chapter audio.", it) }
            .subscribeOn(Schedulers.io())
    }

    private fun appendVerseSegmentsToScratchAudio(segments: VerseSegments) {
        segments.forEach {
            audioFileUtils.appendFile(chapterRepresentation.scratchAudio, it.value)
        }
    }

    private fun createVersesFromVerseSegments(segments: VerseSegments): List<VerseNode> {
        val nodes = mutableListOf<VerseNode>()

        val segmentLabels = segments.keys.map { it.formattedLabel }
        totalVerses
            .filter { marker -> marker.formattedLabel !in segmentLabels }
            .forEach {
                // inserts inactive nodes
                nodes.add(
                    VerseNode(
                        placed = false,
                        marker = it
                    )
                )
            }

        val scratchAudio = chapterRepresentation.scratchAudio
        var start = if (scratchAudio.totalFrames == 0) 0 else scratchAudio.totalFrames + 1
        var end: Int
        val frameSizeInBytes = chapterRepresentation.frameSizeInBytes

        segments.forEach { (marker, file) ->
            val verseAudio = AudioFile(file)
            end = max(start + verseAudio.totalFrames - 1, 0)

            val node = VerseNode(
                true,
                marker,
                mutableListOf(start * frameSizeInBytes until end * frameSizeInBytes)
            )
            nodes.add(node)

            start = end + 1
        }

        return nodes.sortedBy { it.marker.sort } // sort order of book-chapter-verse
    }


    /**
     * Creates a Single that emits a take where the emission of the take is delayed until the take audio is finished
     * bouncing.
     */
    fun createChapterTakeWithAudio(): Single<Take> {
        return chapter.audio.getNewTakeNumber()
            .map { takeNumber ->
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

                take
            }
            .map { take ->
                takeToModify = take

                // bounce to a temp file to avoid Windows file-locking issue when editing with external app
                val tempFile = directoryProvider.createTempFile("bounced-${take.name}", ".wav")
                audioBouncer.bounceAudio(
                    tempFile,
                    chapterRepresentation.getAudioFileReader(),
                    activeVerses
                )
                tempFile.copyTo(take.file, overwrite = true)
                tempFile.deleteOnExit()

                take
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

    private fun getFrameInChapter(): Int {
        return if (lockedVerseIndex != null) {
            chapterReaderConnection
                .frameInVerseToFrameInChapter(player.getLocationInFrames(), lockedVerseIndex!!)
        } else {
            player.getLocationInFrames()
        }
    }

    fun getLocationInFrames(): Int {
        val chapterLocation = getFrameInChapter()
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
        val loc = getFrameInChapter()
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
        val loc = getFrameInChapter()
        lockToVerse(null)

        activeVerses
            .firstOrNull { it.location > loc }
            ?.let {
                logger.info("Seeking to next: ${it.formattedLabel}")
                seek(it.location)
            }
            ?: chapterRepresentation.apply {
                if (activeVerses.isNotEmpty()) {
                    val lastFrame =
                        absoluteFrameToRelativeChapterFrame(activeVerses.last().lastIndex() / frameSizeInBytes)
                    logger.info("Next marker not found, seeking to end of audio; frame: $lastFrame")
                    seek(lastFrame)
                }
            }
    }

    fun findMarkerAtFrame(frame: Int): AudioMarker? {
        val frame =
            chapterRepresentation.relativeChapterFrameToAbsoluteIndex(frame) / chapterRepresentation.frameSizeInBytes
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
