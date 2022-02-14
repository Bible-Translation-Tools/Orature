/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import com.sun.javafx.util.Utils
import java.io.File
import javafx.animation.AnimationTimer
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javax.inject.Inject
import javax.sound.sampled.FloatControl.Type.SAMPLE_RATE
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.ScrollingWaveform
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SECONDS_ON_SCREEN
import tornadofx.*

class Consume : Fragment() {

    val playIcon = FontIcon("mdi-play").apply {iconSize = 36}
    val pauseIcon = FontIcon("mdi-pause").apply {iconSize = 36}

    val vm: ChunkingViewModel by inject()
    // val wkbk: WorkbookDataStore by inject()

    var timer: AnimationTimer? = null

    val waveformImageBuilder = WaveformImageBuilder(wavColor = Color.web("#00153399"))

    override fun onDock() {
        super.onDock()
        tryImportStylesheet(resources.get("/css/verse-marker-app.css"))
        vm.onDock(AudioFile(File("/Users/joe/Documents/test12345.mp3")))
        vm.titleProperty.set("Consume")
        vm.stepProperty.set("Listen to the source audio for chapter ${1}. Pay attention to stories and important events.")

        timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                vm.calculatePosition()
            }
        }
        timer?.start()
    }

    override fun onUndock() {
        super.onUndock()
        timer?.stop()
        vm.compositeDisposable.clear()
    }

    override val root = vbox {
        borderpane {
            alignment = Pos.CENTER
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            center = ScrollingWaveform().apply {
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
            }
        }
        hbox {
            prefHeight = 88.0
            alignment = Pos.CENTER
            style {
                backgroundColor += Paint.valueOf("#00377C")
            }
            button {
//                audioController.isPlayingProperty.onChangeAndDoNow {
//                    it?.let {
//                        when(it) {
//                            true -> graphic = pauseIcon
//                            false -> graphic = playIcon
//                        }
//                    }
//                }
                styleClass.addAll("btn", "btn--cta")
                action {
                  vm.mediaToggle()
                }
                style {
                    prefHeight = 60.px
                    prefWidth = 60.px
                    borderRadius += box(90.px)
                    backgroundRadius += box(90.px)
                }
            }
        }
    }
}

fun pixelsToFrames(pixels: Double): Int {
    val framesOnScreen = SECONDS_ON_SCREEN * 44100
    val framesInPixel = framesOnScreen / Screen.getMainScreen().platformWidth
    return (pixels * framesInPixel).toInt()
}
