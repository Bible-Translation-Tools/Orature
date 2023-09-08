package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import io.reactivex.rxkotlin.addTo
import javafx.scene.Parent
import javafx.scene.control.Slider
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class Chunking : Fragment() {
    private val logger = LoggerFactory.getLogger(javaClass)

    val vm: ChunkingViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()

    private lateinit var waveform: MarkerPlacementWaveform
    private lateinit var slider: Slider

    var cleanUpWaveform: () -> Unit = {}

    override fun onDock() {
        super.onDock()
        logger.info("Chunking docked")

        vm.subscribeOnWaveformImages = ::subscribeOnWaveformImages
        vm.onDockChunking()
        vm.initializeAudioController(slider)
        waveform.markers.bind(vm.markers) { it }
    }

    override fun onUndock() {
        super.onUndock()
        logger.info("Chunking undocked")
        cleanUpWaveform()
        vm.onUndockChunking()
    }

    private fun subscribeOnWaveformImages() {
        vm.waveform
            .observeOnFx()
            .subscribe {
                waveform.addWaveformImage(it)
            }
            .addTo(vm.compositeDisposable)
    }

    override val root = vbox {
        borderpane {
            vgrow = Priority.ALWAYS

            center = VBox().apply {
                MarkerPlacementWaveform().apply {
                    waveform = this
                    addClass("consume__scrolling-waveform")
                    vgrow = Priority.ALWAYS
                    clip = Rectangle().apply {
                        widthProperty().bind(this@vbox.widthProperty())
                        heightProperty().bind(this@vbox.heightProperty())
                    }
                    themeProperty.bind(settingsViewModel.appColorMode)
                    positionProperty.bind(vm.positionProperty)
                    canMoveMarkerProperty.set(true)
                    imageWidthProperty.bind(vm.imageWidthProperty)

                    setUpWaveformActionHandlers()
                    cleanUpWaveform = ::freeImages

                    // Marker stuff
                    this.markers.bind(vm.markers) { it }
                }
                slider = createAudioScrollbarSlider()
                add(waveform)
                add(slider)
            }
            bottom = hbox {
                addClass("consume__bottom")
                button(messages["addChunk"]) {
                    addClass("btn", "btn--primary", "consume__btn")
                    tooltip(text)
                    graphic = FontIcon(MaterialDesign.MDI_PLUS)

                    action {

                    }
                }
                region { hgrow = Priority.ALWAYS }
                hbox {
                    addClass("chunking-bottom__media-btn-group")
                    button {
                        addClass("btn", "btn--icon")
                        graphic = FontIcon(MaterialDesign.MDI_SKIP_PREVIOUS)
                    }
                    button {
                        addClass("btn", "btn--icon")
                        val playIcon = FontIcon(MaterialDesign.MDI_PLAY)
                        val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
                        tooltipProperty().bind(
                            vm.isPlayingProperty.objectBinding {
                                togglePseudoClass("active", it == true)
                                if (it == true) {
                                    graphic = pauseIcon
                                    Tooltip(messages["pause"])
                                } else {
                                    graphic = playIcon
                                    Tooltip(messages["playSource"])
                                }
                            }
                        )

                        action { vm.mediaToggle() }
                    }
                    button {
                        addClass("btn", "btn--icon")
                        graphic = FontIcon(MaterialDesign.MDI_SKIP_NEXT)
                    }
                }
            }
        }
    }

    private fun setUpWaveformActionHandlers() {
        waveform.apply {
            setOnSeekNext { vm.seekNext() }
            setOnSeekPrevious { vm.seekPrevious() }
            setOnPlaceMarker { vm.placeMarker() }
            setOnWaveformClicked { vm.pause() }
            setOnWaveformDragReleased { deltaPos ->
                val deltaFrames = pixelsToFrames(deltaPos)
                val curFrames = vm.getLocationInFrames()
                val duration = vm.getDurationInFrames()
                val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                vm.seek(final)
            }
            setOnRewind(vm::rewind)
            setOnFastForward(vm::fastForward)
            setOnToggleMedia(vm::mediaToggle)
            setOnResumeMedia(vm::resumeMedia)
        }
    }

    private fun createAudioScrollbarSlider(): Slider {
        return AudioSlider().apply {
            hgrow = Priority.ALWAYS
            colorThemeProperty.bind(settingsViewModel.selectedThemeProperty)
            setPixelsInHighlightFunction { vm.pixelsInHighlight(it) }
            player.bind(vm.audioPlayer)
            secondsToHighlightProperty.set(SECONDS_ON_SCREEN)
        }
    }
}