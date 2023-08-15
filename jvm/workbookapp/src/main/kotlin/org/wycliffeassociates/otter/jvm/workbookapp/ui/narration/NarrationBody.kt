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
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.collections.FloatRingBuffer
import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.narration.Narration
import org.wycliffeassociates.otter.common.domain.narration.NarrationFactory
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
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
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class NarrationBody : View() {
    private val viewModel: NarrationBodyViewModel by inject()

    var canvasFragment = CanvasFragment()
    var fps = FramerateView()

    override val root = hbox {

        canvasFragment.prefWidthProperty().bind(this.widthProperty())
        viewModel.isWaveformLayerInitialized.addListener {_, old, new ->
            if(new == true) {
                viewModel.waveformLayer?.heightProperty?.bind(this.heightProperty())
                viewModel.waveformLayer?.widthProperty?.bind(this.widthProperty())
                canvasFragment.drawableProperty.set(viewModel.waveformLayer)
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

    var waveformLayer : NewWaveformLayer? = null
    var narrationWaveformLayer : NarrationWaveformLayer? = null

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

    var isWaveformLayerInitialized = SimpleBooleanProperty(false)
    var isNarrationWaveformLayerInitialized = SimpleBooleanProperty(false)
    fun onDock() {
        workbookDataStore.activeChapterProperty.onChangeAndDoNowWithDisposer {
            it?.let { chapter ->
                initializeNarration(chapter)
            }
        }.let(listeners::add)


        var stream = narration.getRecorderAudioStream()

        val alwaysRecordingStatus: Observable<Boolean> = Observable.just(true)
        val renderer = ActiveRecordingRenderer(stream, alwaysRecordingStatus, 1920, 10)

        waveformLayer = NewWaveformLayer(renderer)
        waveformLayer?.existingAudioReader = narration.audioReader
        waveformLayer?.isRecordingProperty?.bind(isRecordingProperty)
//        isWaveformLayerInitialized.set(true)

        val existingAndIncomingAudioRenderer = ExistingAndIncomingAudioRenderer(narration.audioReader, stream, alwaysRecordingStatus, 1920, 10)
        narrationWaveformLayer = NarrationWaveformLayer(existingAndIncomingAudioRenderer)
        isNarrationWaveformLayerInitialized.set(true)

        isRecordingProperty.addListener {_, old, new ->
            if(old == true && new == false) {
                renderer.clearData()
            }
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


class NewWaveformLayer(var incomingAudioRenderer : ActiveRecordingRenderer) : Drawable {
    var heightProperty = SimpleDoubleProperty(1.0)
    var widthProperty = SimpleDoubleProperty()
    var isRecordingProperty = SimpleBooleanProperty(false)
    var backgroundColor = c("#E5E8EB")
    var waveformColor = c("#B3B9C2")

    val screenWidth = 1920
    val screenHeight = 1080
    val sampleRate = 44100
    val secondsOfAudioToDraw = 10
    val samplesPerPixelWidth =  (sampleRate * secondsOfAudioToDraw) / screenWidth // NOTE: this could result in compounding errors due to rounding. ~= 229

    var previouslyDrawnLines = IntArray(screenWidth * 2) { 0 }
    var writableImage = WritableImage(screenWidth, screenHeight)

    // NOTE: this will be used to hold existing audio PCM data when the AudioFileReader is passed in constructor
    // Can hold 10 seconds worth of PCM data
    private var existingAudio = ByteArray(sampleRate * secondsOfAudioToDraw * 2)
    var existingAudioPosition = 0
    var existingAudioReader : AudioFileReader? = null

    private fun getShort(bytes: ByteArray, position: Int): Short {
        return ((bytes[position + 1].toInt() shl 8) or (bytes[position].toInt() and 0xFF)).toShort()
    }


    fun drawWaveformToImage(context: GraphicsContext, canvas: Canvas) {
        var bytesAvailableFromExisting = 0

        bytesAvailableFromExisting = if(existingAudioReader == null) {
            0
        } else {
            existingAudioReader!!.getPcmBuffer(existingAudio)
        }

        var pxFromIncoming = 0
        if(isRecordingProperty.value == true && incomingAudioRenderer?.floatBuffer != null) {
            pxFromIncoming = incomingAudioRenderer?.floatBuffer?.size()?.div(2)!!
        }

        val pxNeeded = screenWidth - pxFromIncoming
        val samplesAvailableFromExisting = bytesAvailableFromExisting / 2
        val pxAvailableFromExisting = samplesAvailableFromExisting / samplesPerPixelWidth

        var pxOffset = 0
        if(pxAvailableFromExisting < pxNeeded) {
            pxOffset = screenWidth - (pxFromIncoming + pxAvailableFromExisting)
        }

        val pxFromExisting = minOf(pxNeeded, pxAvailableFromExisting)

        drawOffsetToImage(pxOffset, context, canvas)

        drawExistingAudioToImage(pxFromExisting, pxOffset * 2, context, canvas)

        existingAudioPosition = 0 // TODO: possibly remove this. Resets the existingAudioPosition to zero so I can render the same data each time

        context.stroke = Paint.valueOf(Color.GREEN.toString())

        var leftShift = maxOf(0.0, screenWidth - widthProperty.value)
        joesWaveformLayer2?.startingX = (pxOffset + pxFromExisting - leftShift)
        joesWaveformLayer2?.draw(context, canvas)
        context.stroke = Paint.valueOf(Color.BLACK.toString())
    }


    private fun drawVerticalLine(x: Int, startY: Int, endY: Int, color: Color, canvas: Canvas, context: GraphicsContext) {
        context.stroke = waveformColor
        context.lineWidth = 1.0
        context.strokeLine(
            (x - (maxOf(0.0, screenWidth - widthProperty.value))),
            scaleAmplitude(startY.toFloat(), heightProperty.value),
            (x - (maxOf(0.0, screenWidth - widthProperty.value))),
            scaleAmplitude(endY.toFloat(), heightProperty.value)
        )
    }

    private fun drawOffsetToImage(pxOffset: Int, context: GraphicsContext, canvas: Canvas) : Int {
        var currentAmplitude = 0
        for(i in 0 until pxOffset) {
            currentAmplitude += 2
            // remove line that was previously at this position
            drawVerticalLine(
                currentAmplitude / 2 - 1,
                previouslyDrawnLines[currentAmplitude - 2],
                previouslyDrawnLines[currentAmplitude - 1],
                backgroundColor,
                canvas,
                context
            )
        }
        return currentAmplitude
    }

    private fun drawExistingAudioToImage(pxFromExisting: Int, currentAmplitude: Int, context: GraphicsContext, canvas: Canvas) : Int {
        var currentAmplitudeIndex = currentAmplitude
        // get/draw px needed from existing audio
        var min: Float
        var max: Float
        var currentSample: Float
        for(i in 0 until pxFromExisting) {

            min = Short.MAX_VALUE.toFloat()
            max = Short.MIN_VALUE.toFloat()
            for(j in 0 until samplesPerPixelWidth) {
                currentSample = getShort(existingAudio, existingAudioPosition).toFloat()
                existingAudioPosition += 2
                min = minOf(currentSample, min)
                max = maxOf(currentSample, max)
            }
            currentAmplitudeIndex += 2

            // remove line that was previously at this position
            drawVerticalLine(currentAmplitudeIndex / 2 - 1, previouslyDrawnLines[currentAmplitudeIndex - 2], previouslyDrawnLines[currentAmplitudeIndex - 1], backgroundColor, canvas, context)

            val startY = (min).toFloat() + 1
            val endY = (max).toFloat()- 1
            drawVerticalLine(currentAmplitudeIndex / 2 - 1, startY.toInt(), endY.toInt(), waveformColor, canvas, context)

            // Updated with the most recently drawn line
            previouslyDrawnLines[currentAmplitudeIndex - 2] = startY.toInt()
            previouslyDrawnLines[currentAmplitudeIndex - 1] = endY.toInt()
        }
        return currentAmplitudeIndex
    }


    private fun drawIncomingAudioToImage(pxFromIncoming: Int, currentAmplitude: Int, context: GraphicsContext, canvas: Canvas) : Int{
        var currentAmplitudeIndex = currentAmplitude
        // get/draw px available in incoming
        for(i in 0 until pxFromIncoming) {
            currentAmplitudeIndex += 2
            // Remove the line that was previously at this position
            drawVerticalLine(currentAmplitudeIndex / 2 - 1, previouslyDrawnLines[currentAmplitudeIndex - 2], previouslyDrawnLines[currentAmplitudeIndex - 1], backgroundColor, canvas, context)

            var startY = incomingAudioRenderer?.floatBuffer?.get(i)?.plus(1)
            var endY = incomingAudioRenderer?.floatBuffer?.get(i + 1)?.minus(1)
            if(startY == null || endY == null) {
                return 0
            }
            startY = startY.toFloat()
            endY = endY.toFloat()
            drawVerticalLine(currentAmplitudeIndex / 2 - 1, startY.toInt(), endY.toInt(), Color.RED, canvas, context)

            // Updated with the most recently drawn line
            previouslyDrawnLines[currentAmplitudeIndex - 2] = startY.toInt()
            previouslyDrawnLines[currentAmplitudeIndex - 1] = endY.toInt()
        }

        return currentAmplitudeIndex
    }

    private fun scaleAmplitude(sample: Float, height: Double): Double {
        return height / (Short.MAX_VALUE * 2) * (sample + Short.MAX_VALUE)
    }

    var joesWaveformLayer2 : WaveformLayer? = null

    init {
        joesWaveformLayer2 = WaveformLayer(incomingAudioRenderer)
    }

    override fun draw(context: GraphicsContext, canvas: Canvas) {
        drawWaveformToImage(context, canvas)
        context.drawImage(writableImage, (widthProperty.value - screenWidth), 0.0, writableImage.width, canvas.height)
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







class ExistingAndIncomingAudioRenderer(
    val existingAudioReader : AudioFileReader,
    incomingAudioStream : Observable<ByteArray>,
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

    val isRecordingChecker = recordingStatus
        .subscribeOn(Schedulers.io())
        .doOnError {
            logger.error("Error in isRecordingChecker")
        }
        .subscribe {
            if(it == true) {
                println("RECORDING HAS STARTED")
                println("isActive: ${(isActive.get())}")
                // TODO: clear buffer here
            }
        }


    val activeRenderer = incomingAudioStream
        .subscribeOn(Schedulers.io())
        .doOnError { e ->
            logger.error("Error in active renderer stream", e)
        }
        .subscribe {
            println("getting data from stream")
            bb.put(it)
            bb.position(0)


            if(floatBuffer.size() == 0) { // NOTE: for this to work, the floatBuffer MUST be cleared when switched to recording mode
                println("getting offset + existing")
                // fill with offset + existingAudio
                val bytesFromExisting = fillExistingAudioHolder()
                val offset = existingAudioHolder.size - bytesFromExisting

                for(i in 0 until offset) {
                    pcmCompressor.add(0.0F)
                }

                for(i in 0 until (bytesFromExisting - 1) step 2) {
                    val short = ((existingAudioHolder[i + 1].toInt() shl 8) or (existingAudioHolder[i].toInt() and 0xFF)).toShort()
                    pcmCompressor.add(short.toFloat())
                }

            }
            while (bb.hasRemaining()) {
                val short = bb.short
                if (isActive.get()) {
                    pcmCompressor.add(short.toFloat())
                }
            }
            bb.clear()
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
        var bytesFromExisting = existingAudioReader.getPcmBuffer(this.existingAudioHolder)

        return bytesFromExisting
    }

}



class NarrationWaveformLayer(
    val renderer : ExistingAndIncomingAudioRenderer,
) : Drawable {

    var heightProperty = SimpleDoubleProperty(1.0)
    var widthProperty = SimpleDoubleProperty()

    // TODO: possibly update this to implement the Strategy Pattern
    override fun draw(context: GraphicsContext, canvas: Canvas) {
        context.stroke = Paint.valueOf("#1A1A1A")
        context.lineWidth = 1.0

        val buffer = renderer.floatBuffer.array
        var i = 0
        var x = 0.0
        // TODO: add some functionality here that allows me to choose a starting X position
        // The idea is that when there is existing and incoming audio. I don't have 10 seconds of incoming audio
        // so calculate how much existing audio I need, display it, then pass the the starting x position to this,
        // so it can render
        while (i < buffer.size) {
            context.strokeLine(
                (x - (maxOf(0.0, renderer.width - widthProperty.value))),
                scaleAmplitude(buffer[i].toDouble(), canvas.height),
                (x - (maxOf(0.0, renderer.width - widthProperty.value))),
                scaleAmplitude(buffer[i + 1].toDouble(), canvas.height)
            )
            i += 2
            x += 1
        }
    }

    // 16 bit audio range is -32,768 to 32,767, or 65535 (size of unsigned short)
    // This scales the sample to fit within the canvas height, and moves the
    // sample down (-y translate) by half the height
    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height * (sample / UShort.MAX_VALUE.toDouble()) + height / 2
    }
}




class WaveformLayer(private val renderer: ActiveRecordingRenderer) : Drawable {

    var startingX = 0.0
    override fun draw(context: GraphicsContext, canvas: Canvas) {
        context.stroke = Paint.valueOf("#1A1A1A")
        context.lineWidth = 1.0

        val buffer = renderer.floatBuffer.array
        var i = 0
        var x = startingX
        // TODO: add some functionality here that allows me to choose a starting X position
        // The idea is that when there is existing and incoming audio. I don't have 10 seconds of incoming audio
        // so calculate how much existing audio I need, display it, then pass the the starting x position to this,
        // so it can render
        while (i < buffer.size) {
            context.strokeLine(
                x,
                scaleAmplitude(buffer[i].toDouble(), canvas.height),
                x,
                scaleAmplitude(buffer[i + 1].toDouble(), canvas.height)
            )
            i += 2
            x += 1
        }
    }

    // 16 bit audio range is -32,768 to 32,767, or 65535 (size of unsigned short)
    // This scales the sample to fit within the canvas height, and moves the
    // sample down (-y translate) by half the height
    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height * (sample / UShort.MAX_VALUE.toDouble()) + height / 2
    }
}