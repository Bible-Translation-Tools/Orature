package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.RecordScriptureViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.*
import tornadofx.*

private class RecordableViewModelProvider : Component() {
    private val recordScriptureViewModel: RecordScriptureViewModel by inject()
    fun get() = recordScriptureViewModel.recordableViewModel
}

class RecordScriptureFragment : RecordableFragment(
    RecordableViewModelProvider().get(),
    DragTargetBuilder(DragTargetBuilder.Type.SCRIPTURE_TAKE)
) {
    private val recordScriptureViewModel: RecordScriptureViewModel by inject()

    private val takesList = TakesFlowPane(
        recordableViewModel.alternateTakes,
        audioPluginViewModel::audioPlayer,
        lastPlayOrPauseEvent,
        recordableViewModel::recordNewTake
    )

    init {
        importStylesheet<RecordScriptureStyles>()
        importStylesheet<TakeCardStyles>()

        mainContainer.apply {
            addClass(RecordScriptureStyles.background)
            // Top items above the alternate takes
            // Drag target and/or selected take, Next Verse Button, Previous Verse Button
            hbox(15.0) {
                addClass(RecordScriptureStyles.pageTop)
                alignment = Pos.CENTER
                vgrow = Priority.ALWAYS
                // previous verse button
                button(messages["previousVerse"], AppStyles.backIcon()) {
                    addClass(RecordScriptureStyles.navigationButton)
                    action {
                        recordScriptureViewModel.previousChunk()
                    }
                    enableWhen(recordScriptureViewModel.hasPrevious)
                }
                vbox {
                    region {
                        vgrow = Priority.ALWAYS
                    }
                    add(dragTarget)
                    region {
                        vgrow = Priority.ALWAYS
                    }
                }

                // next verse button
                button(messages["nextVerse"], AppStyles.forwardIcon()) {
                    addClass(RecordScriptureStyles.navigationButton)
                    contentDisplay = ContentDisplay.RIGHT
                    action {
                        recordScriptureViewModel.nextChunk()
                        enableWhen(recordScriptureViewModel.hasNext)
                    }
                }
            }

            vbox {
                addClass(RecordScriptureStyles.scrollpaneContainer)
                scrollpane {
                    isFitToWidth = true
                    addClass(RecordScriptureStyles.scrollpane)
                    add(
                        takesList
                    )
                }
            }
        }
    }

    override fun createTakeCard(take: Take): TakeCard {
        return scripturetakecard(
            take,
            audioPluginViewModel.audioPlayer(),
            lastPlayOrPauseEvent.toObservable()
        )
    }
}
