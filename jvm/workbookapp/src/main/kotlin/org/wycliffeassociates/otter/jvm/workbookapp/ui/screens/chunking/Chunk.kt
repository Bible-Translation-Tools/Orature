package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jfoenix.controls.JFXButton
import com.sun.javafx.util.Utils
import java.text.MessageFormat
import javafx.animation.AnimationTimer
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerTrackControl
import org.wycliffeassociates.otter.jvm.controls.waveform.ScrollingWaveform
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.button
import tornadofx.get
import tornadofx.hbox

class Chunk : Fragment() {

    val playIcon = FontIcon("mdi-play")
    val pauseIcon = FontIcon("mdi-pause")
    private val nextIcon = FontIcon("gmi-skip-next")
    private val previousIcon = FontIcon("gmi-skip-previous")

    private val rootStyles = "vm-play-controls"
    private val playButtonStyle = "vm-play-controls__play-btn"
    private val roundedButtonStyle = "vm-play-controls__btn--rounded"
    private val seekButtonStyle = "vm-play-controls__seek-btn"
    private val continueButtonStyle = "vm-continue-button"

    val vm: ChunkingViewModel by inject()

    private val markerTrack: MarkerTrackControl = MarkerTrackControl()

    var timer: AnimationTimer? = null

    override fun onDock() {
        super.onDock()
        tryImportStylesheet(resources.get("/css/verse-marker-app.css"))
        tryImportStylesheet(resources.get("/css/chunk-marker.css"))
        tryImportStylesheet(resources.get("/css/consume-page.css"))
        vm.onDockConsume()
        vm.titleProperty.set("Chunking")
        vm.stepProperty.set(MessageFormat.format(messages["consumeDescription"], vm.chapterTitle))

        timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                vm.calculatePosition()
            }
        }
        timer?.start()
        vm.onDockChunk()

        markerTrack.apply {
            prefWidth = vm.imageWidth
            vm.markerStateProperty.onChangeAndDoNow { markers ->
                markers?.let { markers ->
                    markers.markerCountProperty?.onChangeAndDoNow {
                        highlightState.setAll(vm.markers.highlightState)
                        this.markers.setAll(vm.markers.markers)
                        refreshMarkers()
                    }
                }
            }
        }
    }

    override fun onUndock() {
        super.onUndock()
        timer?.stop()
        vm.compositeDisposable.clear()
    }

    override val root = borderpane {
        center = MarkerPlacementWaveform(markerTrack).apply {
            positionProperty.bind(vm.positionProperty)

            vm.compositeDisposable.add(
                vm.waveform.observeOnFx().subscribe { addWaveformImage(it) }
            )

            onWaveformClicked = { vm.pause() }
            onWaveformDragReleased = { deltaPos ->
                val deltaFrames = pixelsToFrames(deltaPos)
                val curFrames = vm.getLocationInFrames()
                val duration = vm.getDurationInFrames()
                val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                vm.seek(final)
            }

            onPlaceMarker = vm::placeMarker

            markerStateProperty.bind(vm.markerStateProperty)
        }
        bottom = hbox {
            styleClass.addAll("consume__bottom")
            styleClass.add(rootStyles)
            alignment = Pos.CENTER
            val nextBtn = JFXButton().apply {
                graphic = nextIcon
                setOnAction { vm.seekNext() }
                styleClass.addAll(
                    seekButtonStyle,
                    roundedButtonStyle
                )
            }

            val previousBtn = JFXButton().apply {
                graphic = previousIcon
                setOnAction { vm.seekPrevious() }
                styleClass.addAll(
                    seekButtonStyle,
                    roundedButtonStyle
                )
            }
            add(previousBtn)

            button {
                vm.isPlayingProperty.onChangeAndDoNow {
                    it?.let {
                        when (it) {
                            true -> graphic = pauseIcon
                            false -> graphic = playIcon
                        }
                    }
                }
                styleClass.addAll(
                    playButtonStyle,
                    roundedButtonStyle
                )
                action {
                    vm.mediaToggle()
                }
            }
            add(nextBtn)
        }
    }
}

