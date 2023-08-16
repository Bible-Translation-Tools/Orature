package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.collections.FloatRingBuffer
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.narration.Narration
import org.wycliffeassociates.otter.common.domain.narration.NarrationFactory
import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import org.wycliffeassociates.otter.common.recorder.PCMCompressor
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.narration.CanvasFragment
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import org.wycliffeassociates.otter.jvm.controls.waveform.VolumeBar
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
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


class NarrationBody : View() {
    private val viewModel: NarrationBodyViewModel by inject()

    var canvasFragment = CanvasFragment()
    var volumeBarCanavsFragment = CanvasFragment()
    var fps = FramerateView()

    override val root = hbox {
        this.maxWidth = 1895.0
        canvasFragment.prefWidthProperty().bind(this.widthProperty().minus(25))
        canvasFragment.maxWidth(1920.0)
        canvasFragment.let {
            style {
                backgroundColor += c("#E5E8EB")
            }
        }

        viewModel.isNarrationWaveformLayerInitialized.addListener {_, old, new ->
            if(new == true) {
                viewModel.narrationWaveformLayer?.heightProperty?.bind(this.heightProperty())
                viewModel.narrationWaveformLayer?.widthProperty?.bind(this.widthProperty())
                canvasFragment.drawableProperty.set(viewModel.narrationWaveformLayer)
            }
        }
        canvasFragment.isDrawingProperty.set(true)
        canvasFragment.add(fps)

        add(canvasFragment)

        hbox {
            prefWidth = 25.0
            volumeBarCanavsFragment.let {
                style {
                    backgroundColor += c("#001533")
                }
            }
            volumeBarCanavsFragment.prefWidthProperty().bind(this.widthProperty())
            viewModel.isNarrationWaveformLayerInitialized.addListener {_, old, new ->
                if(new == true) {
                    volumeBarCanavsFragment.drawableProperty.set(viewModel.volumeBar)
                    volumeBarCanavsFragment.isDrawingProperty.set(true)
                }
            }
            add(volumeBarCanavsFragment)
        }

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

    var narrationWaveformLayer : NarrationWaveformLayer? = null
    var volumeBar : VolumeBar? = null

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

    var isNarrationWaveformLayerInitialized = SimpleBooleanProperty(false)
    fun onDock() {
        workbookDataStore.activeChapterProperty.onChangeAndDoNowWithDisposer {
            it?.let { chapter ->
                initializeNarration(chapter)
            }
        }.let(listeners::add)


        val stream = narration.getRecorderAudioStream()
        val alwaysRecordingStatus: Observable<Boolean> = Observable.just(true)
        val existingAndIncomingAudioRenderer = ExistingAndIncomingAudioRenderer(narration.audioReader, stream, alwaysRecordingStatus, 1920, 10)
        narrationWaveformLayer = ImageNarrationWaveformLayer(existingAndIncomingAudioRenderer)
        narrationWaveformLayer!!.isRecordingProperty.bind(isRecordingProperty)

        volumeBar = VolumeBar(stream)
        isNarrationWaveformLayerInitialized.set(true)

        isRecordingProperty.addListener {_, old, new ->
            existingAndIncomingAudioRenderer.clearData()
        }
    }

    fun onUndock() {
        listeners.forEach(ListenerDisposer::dispose)
        disposables.dispose()

        closeNarrationAudio()
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
        narrationWaveformLayer?.renderer?.clearData()
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

    private fun closeNarrationAudio() {
        narration.closeRecorder()
        narration.closeChapterRepresentation()
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







class ExistingAndIncomingAudioRenderer(
    val existingAudioReader : AudioFileReader,
    val incomingAudioStream : Observable<ByteArray>,
    recordingStatus: Observable<Boolean>,
    val width: Int,
    secondsOnScreen: Int) {

    private val DEFAULT_BUFFER_SIZE = 1024
    private val logger = LoggerFactory.getLogger(ActiveRecordingRenderer::class.java)

    private var isActive = AtomicBoolean(false)
    private var recordingActive: Observable<Boolean> = recordingStatus

    // double the width as for each pixel there will be a min and max value
    val floatBuffer = FloatRingBuffer(width * 2)
    private val pcmCompressor = PCMCompressor(floatBuffer, samplesToCompress(width, secondsOnScreen))
    val bb = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)

    val compositeDisposable = CompositeDisposable()

    val existingAudioHolder = ByteArray(DEFAULT_SAMPLE_RATE * secondsOnScreen * 2)

    init {
        recordingActive
            .doOnError { e ->
                logger.error("Error in active recording listener", e)
            }
            .subscribe { isActive.set(it) }
            .also {
                compositeDisposable.add(it)
            }
        bb.order(ByteOrder.LITTLE_ENDIAN)
    }

    // NOTE: I would like to subscribe to something (possibly the position of the play-head) that will call
    // existingAudioReader.getPCMBuffer / fillExistingAudioHolder

    val activeRenderer = incomingAudioStream
        .subscribeOn(Schedulers.io())
        .doOnError { e ->
            logger.error("Error in active renderer stream", e)
        }
        .subscribe {
            if(isActive.get()) {
                bb.put(it)
                bb.position(0)


                if(floatBuffer.size() == 0) { // NOTE: for this to work, the floatBuffer MUST be cleared when switched to recording mode
                    // fill with offset + existingAudio
                    fillExistingAudioHolder()
                }

                while (bb.hasRemaining()) {
                    val short = bb.short
                    if (isActive.get()) {
                        pcmCompressor.add(short.toFloat())
                    }
                }
                bb.clear()
            }

        }

    private fun samplesToCompress(width: Int, secondsOnScreen: Int): Int {
        // TODO: get samplerate from wav file, don't assume 44.1khz
        return (DEFAULT_SAMPLE_RATE * secondsOnScreen) / width
    }

    /** Sets a new status listener and removes the old one */
    fun setRecordingStatusObservable(value: Observable<Boolean>) {
        compositeDisposable.clear()

        recordingActive = value
        recordingActive
            .doOnError { e ->
                logger.error("Error in active recording listener", e)
            }
            .subscribe { isActive.set(it) }
            .also {
                compositeDisposable.add(it)
            }
    }

    /** Clears rendered data from buffer */
    fun clearData() {
        floatBuffer.clear()
    }

    fun fillExistingAudioHolder(): Int {
        val bytesFromExisting = existingAudioReader.getPcmBuffer(this.existingAudioHolder)
        val offset = existingAudioHolder.size - bytesFromExisting

        var i = 0
        while( i < offset) {
            pcmCompressor.add(0.0F)
            i++
        }

        i = 0
        while(i < bytesFromExisting - 1) {
            val short = ((existingAudioHolder[i + 1].toInt() shl 8) or (existingAudioHolder[i].toInt() and 0xFF)).toShort()
            pcmCompressor.add(short.toFloat())
            i+=2
        }

        return bytesFromExisting
    }
}



open class NarrationWaveformLayer(
    val renderer : ExistingAndIncomingAudioRenderer
) : Drawable {

    val heightProperty = SimpleDoubleProperty(1.0)
    val widthProperty = SimpleDoubleProperty()
    val isRecordingProperty = SimpleBooleanProperty(false)
    val DEFAULT_SCREEN_WIDTH = 1920
    val DEFAULT_SCREEN_HEIGHT = 1080
    val backgroundColor = c("#E5E8EB")
    val waveformColor = c("#66768B")

    override fun draw(context: GraphicsContext, canvas: Canvas) {

        // NOTE: This is what is causing the flicker
        // Possibly, a better solution would be to make the render responsible for updating itself depending on the
        // position of the play-head. This would also greatly reduce the work on the CPU, since it won't have to generate
        // the same data repeatedly even when the play-head has not moved
//        if(isRecordingProperty.value == false) {
//            renderer.fillExistingAudioHolder()
//        }
        context.stroke = waveformColor
        context.lineWidth = 1.0

        var i = 0
        var x = 0.0
        var y1 = 0.0
        var y2 = 0.0
        // The idea is that when there is existing and incoming audio. I don't have 10 seconds of incoming audio
        // so calculate how much existing audio I need, display it, then pass the starting x position to this,
        // so it can render
        while (i < renderer.floatBuffer.array.size) {
            y1 = scaleAmplitude(renderer.floatBuffer.array[i].toDouble(), canvas.height)
            y2 = scaleAmplitude(renderer.floatBuffer.array[i + 1].toDouble(), canvas.height)

            context.strokeLine(
                (x - (maxOf(0.0, renderer.width - widthProperty.value))),
                y1,
                (x - (maxOf(0.0, renderer.width - widthProperty.value))),
                y2
            )
            i += 2
            x += 1
        }
    }

    // 16-bit audio range is -32,768 to 32,767, or 65535 (size of unsigned short)
    // This scales the sample to fit within the canvas height, and moves the
    // sample down (-y translate) by half the height
    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height * (sample / UShort.MAX_VALUE.toDouble()) + height / 2
    }
}

















class ImageNarrationWaveformLayer(renderer: ExistingAndIncomingAudioRenderer) : NarrationWaveformLayer(renderer) {
    val writableImage = WritableImage(DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT)
    val pixelWriter = writableImage.pixelWriter
    var pixelFormat: PixelFormat<ByteBuffer> = PixelFormat.getByteRgbInstance()
    private val imageData = ByteArray(DEFAULT_SCREEN_WIDTH * DEFAULT_SCREEN_HEIGHT * 3)

    override fun draw(context: GraphicsContext, canvas: Canvas) {
        val buffer = renderer.floatBuffer.array

        fillImageDataWithDefaultColor()
        addLinesToImageData(buffer)
        drawImageDataToImage()

        context.drawImage(writableImage, 0.0, 0.0, canvas.width, DEFAULT_SCREEN_HEIGHT.toDouble())
    }

    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height / (Short.MAX_VALUE * 2) * (sample + Short.MAX_VALUE)
    }

    fun fillImageDataWithDefaultColor() {
        var i = 0;
        for (y in 0 until DEFAULT_SCREEN_HEIGHT) {
            for ( x in 0 until DEFAULT_SCREEN_WIDTH) {
                imageData[i] = (backgroundColor.red * 255).toInt().toByte()
                imageData[i + 1] = (backgroundColor.green * 255).toInt().toByte()
                imageData[i + 2] = (backgroundColor.blue * 255).toInt().toByte()
                i += 3
            }
        }
    }

    fun addLinesToImageData(buffer: FloatArray) {
        for (x in 0 until buffer.size / 2) {
            val y1 = scaleAmplitude(buffer[x * 2].toDouble(), heightProperty.value)
            val y2 = scaleAmplitude(buffer[x * 2 + 1].toDouble(), heightProperty.value)

            for (y in minOf(y1.toInt(), y2.toInt())..maxOf(y1.toInt(), y2.toInt())) {
                imageData[(x + y * DEFAULT_SCREEN_WIDTH) * 3] = (waveformColor.red * 255).toInt().toByte()
                imageData[(x + y * DEFAULT_SCREEN_WIDTH) * 3 + 1] = (waveformColor.green * 255).toInt().toByte()
                imageData[(x + y * DEFAULT_SCREEN_WIDTH) * 3 + 2] = (waveformColor.blue * 255).toInt().toByte()
            }
        }
    }

    fun drawImageDataToImage() {
        pixelWriter.setPixels(0, 0, DEFAULT_SCREEN_WIDTH,
            DEFAULT_SCREEN_HEIGHT, pixelFormat, imageData,
            0, DEFAULT_SCREEN_WIDTH * 3)
    }

    init {
        fillImageDataWithDefaultColor()
    }
}
