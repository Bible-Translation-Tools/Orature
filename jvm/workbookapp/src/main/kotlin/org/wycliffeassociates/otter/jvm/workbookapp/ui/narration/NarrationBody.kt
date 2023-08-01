package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.collections.FloatRingBuffer
import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.narration.Narration
import org.wycliffeassociates.otter.common.domain.narration.NarrationFactory
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.common.recorder.PCMCompressor
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.narration.CanvasFragment
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NextVerseEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.RecordVerseEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationRedoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationResetChapterEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationUndoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.function.DoubleUnaryOperator
import javax.inject.Inject

class NarrationBody : View() {
    private val viewModel: NarrationBodyViewModel by inject()

    var canvasFragment = CanvasFragment()
    var fps = FramerateView()

    override val root = hbox {
        var waveform = DrawableWaveForm()

        runAsync {
            waveform.drawAllLocalMinAndMaxToImage()
        }

        waveform.heightProperty.bind(this.heightProperty())
        waveform.widthProperty.bind(this.widthProperty())
        canvasFragment.prefWidthProperty().bind(this.widthProperty())
        canvasFragment.drawableProperty.set(waveform)
        canvasFragment.isDrawingProperty.set(true)
        canvasFragment.add(fps)
        add(canvasFragment)

    }
    init {
        subscribe<NarrationUndoEvent> {
            viewModel.undo()
        }

        subscribe<NarrationRedoEvent> {
            viewModel.redo()
        }

        subscribe<NarrationResetChapterEvent> {
            viewModel.resetChapter()
        }

        subscribe<RecordVerseEvent> {
            viewModel.toggleRecording()
        }

        subscribe<NextVerseEvent> {
            viewModel.onNext()
        }

        subscribe<PlayVerseEvent> {
            viewModel.play(it.verse)
        }

        subscribe<RecordAgainEvent> {
            viewModel.recordAgain(it.index)
        }

        subscribe<OpenInAudioPluginEvent> {
            viewModel.openInAudioPlugin(it.index)
        }

        subscribe<ChapterReturnFromPluginEvent> {
            viewModel.onChapterReturnFromPlugin()
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.onDock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.onUndock()
    }
}

class NarrationBodyViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(NarrationBodyViewModel::class.java)

    @Inject lateinit var narrationFactory: NarrationFactory

    private val workbookDataStore: WorkbookDataStore by inject()
    private val narrationViewViewModel: NarrationViewViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    private val audioController = AudioPlayerController(Slider())

    private val recordStartProperty = SimpleBooleanProperty()
    private var recordStart by recordStartProperty

    private val recordPauseProperty = SimpleBooleanProperty()
    private var recordPause by recordPauseProperty

    private val recordResumeProperty = SimpleBooleanProperty()
    private var recordResume by recordResumeProperty

    private val potentiallyFinishedProperty = SimpleBooleanProperty()
    private var potentiallyFinished by potentiallyFinishedProperty

    val isRecordingProperty = SimpleBooleanProperty()
    private var isRecording by isRecordingProperty

    val isRecordingAgainProperty = SimpleBooleanProperty()
    private var isRecordingAgain by isRecordingAgainProperty

    private val recordAgainVerseIndexProperty = SimpleObjectProperty<Int?>()
    private var recordAgainVerseIndex by recordAgainVerseIndexProperty

    private val playingVerseProperty = SimpleObjectProperty<VerseNode?>()
    private var playingVerse by playingVerseProperty

    private val hasUndoProperty = SimpleBooleanProperty()
    private var hasUndo by hasUndoProperty

    private val hasRedoProperty = SimpleBooleanProperty()
    private var hasRedo by hasRedoProperty

    private lateinit var narration: Narration

    val pluginContextProperty = SimpleObjectProperty(PluginType.EDITOR)

    val recordedVerses = observableListOf<VerseNode>()

    private val listeners = mutableListOf<ListenerDisposer>()
    private val disposables = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        narrationViewViewModel.recordStartProperty.bind(recordStartProperty)
        narrationViewViewModel.recordResumeProperty.bind(recordResumeProperty)
        narrationViewViewModel.isRecordingProperty.bind(isRecordingProperty)
        narrationViewViewModel.recordPauseProperty.bind(recordPauseProperty)
        narrationViewViewModel.isRecordingAgainProperty.bind(isRecordingAgainProperty)

        narrationViewViewModel.hasUndoProperty.bind(hasUndoProperty)
        narrationViewViewModel.hasRedoProperty.bind(hasRedoProperty)
        narrationViewViewModel.hasVersesProperty.bind(recordedVerses.booleanBinding { it.isNotEmpty() })

        narrationViewViewModel.lastRecordedVerseProperty.bind(recordedVerses.integerBinding { it.size })
    }

    fun onDock() {
        workbookDataStore.activeChapterProperty.onChangeAndDoNowWithDisposer {
            it?.let { chapter ->
                initializeNarration(chapter)
            }
        }.let(listeners::add)
    }

    fun onUndock() {
        listeners.forEach(ListenerDisposer::dispose)
        disposables.dispose()

        closeRecorder()
    }

    fun play(verse: VerseNode) {
        if (playingVerse == verse) {
            audioController.toggle()
        } else {
            audioController.pause()

            narration.loadSectionIntoPlayer(verse)

            audioController.load(narration.getPlayer())
            audioController.seek(0)
            audioController.play()

            playingVerse = verse
        }
    }

    fun recordAgain(verseIndex: Int) {
        stopPlayer()

        narration.onRecordAgain(verseIndex)

        recordAgainVerseIndex = verseIndex
        isRecording = true
        isRecordingAgain = true
        recordPause = false
    }

    fun openInAudioPlugin(index: Int) {
        val file = narration.getSectionAsFile(index)
        processWithEditor(file, index)
    }

    fun onChapterReturnFromPlugin() {
        reloadNarration()
    }

    fun onNext() {
        when {
            isRecording -> {
                narration.finalizeVerse()
                narration.onNewVerse()
            }
            recordPause -> {
                recordPause = false
                recordResume = true
            }
            else -> {}
        }
    }

    fun toggleRecording() {
        when {
            isRecording && !isRecordingAgain -> pauseRecording()
            isRecording && isRecordingAgain -> stopRecordAgain()
            recordPause -> resumeRecording()
            recordStart || recordResume -> record()
            else -> {}
        }
    }

    fun resetChapter() {
        narration.onResetAll()

        recordStart = true
        recordResume = false
        recordPause = false
    }

    fun undo() {
        narration.undo()

        recordPause = false
    }

    fun redo() {
        narration.redo()

        recordPause = false
    }

    private fun initializeNarration(chapter: Chapter) {
        Completable.fromAction {
            narration = narrationFactory.create(workbookDataStore.workbook, chapter)
        }
            .doOnError {
                logger.error("An error occurred in loadNarration", it)
            }
            .subscribe {
                subscribeActiveVersesChanged()

                recordStart = recordedVerses.isEmpty()
                recordResume = recordedVerses.isNotEmpty()
                potentiallyFinished = checkPotentiallyFinished()
            }
    }

    private fun reloadNarration() {
        Completable.fromCallable {
            narration.loadFromSelectedChapterFile()
        }
            .doOnError {
                logger.error("An error occurred in loadNarration", it)
            }
            .subscribe {
                recordedVerses.setAll(narration.activeVerses)

                recordStart = recordedVerses.isEmpty()
                recordResume = recordedVerses.isNotEmpty()
                potentiallyFinished = checkPotentiallyFinished()
            }
    }

    private fun record() {
        stopPlayer()

        narration.onNewVerse()

        isRecording = true
        recordStart = false
        recordResume = false
    }

    private fun pauseRecording() {
        isRecording = false
        recordPause = true

        narration.pauseRecording()
        narration.finalizeVerse()

        potentiallyFinished = checkPotentiallyFinished()
    }

    private fun resumeRecording() {
        stopPlayer()

        narration.resumeRecording()

        isRecording = true
        recordPause = false
    }

    private fun stopRecordAgain() {
        recordAgainVerseIndex?.let { verseIndex ->
            narration.pauseRecording()
            narration.finalizeVerse(verseIndex)

            recordAgainVerseIndex = null
            isRecording = false
            isRecordingAgain = false

            recordPause = false
            recordResume = true
        }
    }

    private fun stopPlayer() {
        audioController.pause()
        playingVerse = null
    }

    private fun closeRecorder() {
        narration.closeRecorder()
    }

    private fun processWithEditor(file: File, verseIndex: Int) {
        val pluginType = PluginType.EDITOR
        pluginContextProperty.set(pluginType)

        audioPluginViewModel.getPlugin(pluginType)
            .doOnError { e ->
                logger.error("Error in processing take with plugin type: $pluginType, ${e.message}")
            }
            .flatMapSingle { plugin ->
                workbookDataStore.activeTakeNumberProperty.set(1)
                FX.eventbus.fire(PluginOpenedEvent(pluginType, plugin.isNativePlugin()))
                audioPluginViewModel.edit(file)
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in processing take with plugin type: $pluginType - $e")
            }
            .onErrorReturn { PluginActions.Result.NO_PLUGIN }
            .subscribe { result ->
                when (result) {
                    PluginActions.Result.NO_PLUGIN -> {
                        FX.eventbus.fire(SnackBarEvent(messages["noEditor"]))
                    }
                    else -> {
                        narration.onEditVerse(verseIndex, file)
                    }
                }
                FX.eventbus.fire(PluginClosedEvent(pluginType))
            }
    }

    private fun checkPotentiallyFinished(): Boolean {
        return workbookDataStore.chapter.chunkCount.blockingGet() == recordedVerses.size
    }

    private fun subscribeActiveVersesChanged() {
        recordedVerses.setAll(narration.activeVerses)
        hasUndo = narration.hasUndo()
        hasRedo = narration.hasRedo()

        narration.onActiveVersesUpdated.subscribe {
            recordedVerses.setAll(it)

            hasUndo = narration.hasUndo()
            hasRedo = narration.hasRedo()

            recordStart = recordedVerses.isEmpty()
            recordResume = recordedVerses.isNotEmpty()
        }.let(disposables::add)
    }
}

class RecordAgainEvent(val index: Int) : FXEvent()
class PlayVerseEvent(val verse: VerseNode) : FXEvent()
class OpenInAudioPluginEvent(val index: Int) : FXEvent()

class ChapterReturnFromPluginEvent: FXEvent()






class DrawableWaveForm() : Drawable {

    // TODO: inject narration and use the pcmBuffer to display the current audio data

    var heightProperty = SimpleDoubleProperty(1.0)
    var widthProperty = SimpleDoubleProperty(1.0)
    val waveformColor = Color.rgb(26, 26, 26)
    var backgroundColor = c("#E5E8EB")

    val screenWidth = 1920
    val screenHeight = 1080
    val sampleRate = 44100
    val samplesPerPixelWidth =  (sampleRate * 10) / screenWidth // NOTE: this could result in compounding errors due to rounding. ~= 229
    var samplesPerLine = samplesPerPixelWidth

    var samplesBuffer = FloatArray(sampleRate * 10)
    var allLocalMinAndMaxSamples = FloatArray(screenWidth * 2) { 0.0F }
    var previouslyDrawnLines = IntArray(screenWidth * 2) { 0 }
    var writableImage = WritableImage(screenWidth, screenHeight)

    private var isWaveformDirty = true


    // TODO: replace with PCMCompressor
    fun findAllLocalMinAndMaxSamples(samplesBuffer: FloatArray) {
        var i = 0
        var position = 0
        while(i < samplesBuffer.size) {

            var min = (Short.MAX_VALUE + 1).toFloat()
            var max = (Short.MIN_VALUE - 1).toFloat()
            for (j in i until (i + samplesPerLine)) {

                if(j >= samplesBuffer.size) break

                if(samplesBuffer[j] < min) min = samplesBuffer[j]

                if(samplesBuffer[j] > max) max = samplesBuffer[j]
            }
            allLocalMinAndMaxSamples[position] = min
            allLocalMinAndMaxSamples[position + 1] = max
            i += samplesPerLine + 1
            position += 2
        }
    }


    private fun scaleAmplitude(sample: Float, height: Double): Double {
        return height / (Short.MAX_VALUE * 2) * (sample + Short.MAX_VALUE)
    }


    fun drawAllLocalMinAndMaxToImage() {
        if (!isWaveformDirty) return

        val pixelWriter = writableImage.pixelWriter

        // Clear only the lines that need to be redrawn
        for (i in previouslyDrawnLines.indices step 2) {
            val startX = i / 2
            val startY = previouslyDrawnLines[i]
            val endY = previouslyDrawnLines[i + 1]
            for (y in startY..endY) {
                pixelWriter.setColor(startX, y, backgroundColor)
            }
        }

        // Draw the updated waveform
        var x = 0
        var x2 = 0
        while (x < writableImage.width.toInt() && x < allLocalMinAndMaxSamples.size - 1) {
            val y1 = scaleAmplitude(allLocalMinAndMaxSamples[x], writableImage.height).toInt()
            val y2 = scaleAmplitude(allLocalMinAndMaxSamples[x + 1], writableImage.height).toInt()

            val startY = y1 + 1
            val endY = y2 - 1

            for (y in startY..endY) {
                pixelWriter.setColor(x, y, waveformColor)
            }

            previouslyDrawnLines[x2] = startY
            previouslyDrawnLines[x2 + 1] = endY

            x++
            x2 += 2
        }

        // Mark the waveform as not dirty, as we have now drawn the updated parts
        isWaveformDirty = false
    }


    var waveformGenerator = WaveformGenerator()

    override fun draw(context: GraphicsContext, canvas: Canvas) {
        waveformGenerator.fillAmplitudes(samplesBuffer)
        findAllLocalMinAndMaxSamples(samplesBuffer)
        drawAllLocalMinAndMaxToImage()
        isWaveformDirty = true
        context.drawImage(writableImage, 0.0, 0.0, writableImage.width, canvas.height)
    }


}



class WaveformGenerator {
    val sampleRate = 44100

    // NOTE: used to generate test data. Will be removed once I have access to
    // the workingAudio's AudioFileReader
    var existingAudio = ByteBuffer.allocate(sampleRate * 10 * 2)

    // NOTE: used to generate ring buffer for incoming data.
    var incomingAudio = ByteBuffer.allocate(sampleRate * 10 * 2)

    // NOTE: used to generate test data. Will be removed once I have access to the
    // AudioRecorder's FloatRingBuffer generated by its PcmCompressor method.
    var incomingAudioRingBuffer = FloatRingBuffer(sampleRate * 10)


    fun fillAmplitudes(amplitudes : FloatArray) {
        val amplitudesFromIncoming = minOf(amplitudes.size, incomingAudioRingBuffer.size())

        val remainingSamples = amplitudes.size - amplitudesFromIncoming

        var pos = 0
        var i = 0

        while(i < remainingSamples / 2) {
            // get amplitudes from existing audio here
            amplitudes[pos] = existingAudio.getShort().toFloat()
            i++
            pos++
        }

        i = 0
        while (i < amplitudesFromIncoming) {
            amplitudes[pos] = incomingAudioRingBuffer.get(i)
            i++
            pos++
        }

        existingAudio.position(0) // TODO: remove after testing. The position of the existing Audio will be specified by the playhead
    }


    var sampleGeneratorSeed = 0.0
    fun fillTestSamplesBuffer(
        samplesBuffer: ByteBuffer,
        secondsToDisplay: Int,
        operation: DoubleUnaryOperator)
    {
        var sampleValue = 0.0
        var j = 0
        for(i in 1 ..  (sampleRate*secondsToDisplay)) {
            sampleValue = Short.MAX_VALUE * operation.applyAsDouble(sampleGeneratorSeed)
            samplesBuffer.put((sampleValue.toInt() and 0xFF).toByte())
            samplesBuffer.put((sampleValue.toInt() ushr 8 and 0xFF).toByte())
            sampleGeneratorSeed  += 0.0001
            j += 2
        }
        samplesBuffer.position(0)
        samplesBuffer.limit(j)
    }


    init {
        fillTestSamplesBuffer(incomingAudio, 4) { Math.cos(it) }
        fillTestSamplesBuffer(existingAudio, 6) { Math.sin(it) }

        incomingAudio.order(ByteOrder.LITTLE_ENDIAN)
        existingAudio.order(ByteOrder.LITTLE_ENDIAN)

        while (incomingAudio.remaining() >= 2) {
            val sampleValue = incomingAudio.getShort().toFloat()
            incomingAudioRingBuffer.add(sampleValue)
        }
        incomingAudio.position(0)
    }

}











class FramerateView : Label() {

    private val frameTimes = LongArray(100)
    private var frameTimeIndex = 0
    private var arrayFilled = false

    private val builder = StringBuilder("FPS: 000")

    // code from:
    // https://stackoverflow.com/questions/28287398/what-is-the-preferred-way-of-getting-the-frame-rate-of-a-javafx-application
    val at = object : AnimationTimer() {

        override fun handle(currentNanoTime: Long) {
            val oldFrameTime = frameTimes[frameTimeIndex]
            frameTimes[frameTimeIndex] = currentNanoTime
            frameTimeIndex = (frameTimeIndex + 1) % frameTimes.size
            if (frameTimeIndex == 0) {
                arrayFilled = true
            }
            if (arrayFilled) {
                val elapsedNanos = currentNanoTime - oldFrameTime
                val elapsedNanosPerFrame = elapsedNanos / frameTimes.size
                val frameRate = 1_000_000_000 / elapsedNanosPerFrame
                builder.replace(5, 8, frameRate.toString())
                text = builder.toString()
            }
        }
    }.start()

    init {
        prefHeight = 50.0
        prefWidth = 100.0
        alignment = Pos.TOP_LEFT
        textFill = Paint.valueOf("#00FF00")
    }
}

