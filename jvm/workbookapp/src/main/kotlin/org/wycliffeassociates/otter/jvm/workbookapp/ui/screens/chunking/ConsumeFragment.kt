package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.sun.javafx.util.Utils
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.waveform.ScrollingWaveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import tornadofx.*

class ConsumeFragment : Fragment() {
    val vm: ChunkingViewModel by inject()

    override val root = VBox().apply {
        ScrollingWaveform().apply {
            addClass("consume__scrolling-waveform")

//            themeProperty.bind(settingsViewModel.appColorMode)
//            positionProperty.bind(vm.positionProperty)

//            setOnWaveformClicked { vm.pause() }
//            setOnWaveformDragReleased { deltaPos ->
//                val deltaFrames = pixelsToFrames(deltaPos)
//                val curFrames = vm.getLocationInFrames()
//                val duration = vm.getDurationInFrames()
//                val final = Utils.clamp(0, curFrames - deltaFrames, duration)
//                vm.seek(final)
//            }

//            setOnToggleMedia(vm::mediaToggle)
//            setOnRewind(vm::rewind)
//            setOnFastForward(vm::fastForward)
//
//            vm.consumeImageCleanup = ::freeImages
        }
    }
}