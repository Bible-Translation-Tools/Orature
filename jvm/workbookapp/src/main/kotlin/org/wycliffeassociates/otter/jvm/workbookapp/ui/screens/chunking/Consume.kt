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
import java.text.MessageFormat
import javafx.animation.AnimationTimer
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.ScrollingWaveform
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingWizardPage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SECONDS_ON_SCREEN
import tornadofx.*

class Consume : Fragment() {

    val playIcon = FontIcon("mdi-play")
    val pauseIcon = FontIcon("mdi-pause")

    val vm: ChunkingViewModel by inject()

    var timer: AnimationTimer? = null

    override fun onDock() {
        super.onDock()
        tryImportStylesheet(resources.get("/css/scrolling-waveform.css"))
        tryImportStylesheet(resources.get("/css/consume-page.css"))
        vm.onDockConsume()
        vm.pageProperty.set(ChunkingWizardPage.CONSUME)
        vm.titleProperty.set(messages["consumeTitle"])
        vm.stepProperty.set(MessageFormat.format(messages["consumeDescription"], vm.chapterTitle))

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

    override val root = borderpane {
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
        bottom = hbox {
            styleClass.addAll("consume__bottom")
            button {
                vm.isPlayingProperty.onChangeAndDoNow {
                    it?.let {
                        when (it) {
                            true -> graphic = pauseIcon
                            false -> graphic = playIcon
                        }
                    }
                }
                styleClass.addAll("btn", "btn--cta", "consume__btn--primary")
                action {
                    vm.mediaToggle()
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
