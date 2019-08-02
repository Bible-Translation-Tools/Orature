package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordScriptureViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.dragtarget.DragTarget
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.*
import tornadofx.*

private class RecordableViewModelProvider: Component() {
    private val recordScriptureViewModel: RecordScriptureViewModel by inject()
    fun get() = recordScriptureViewModel.recordableViewModel
}

class RecordScriptureFragment
    : RecordableFragment(RecordableViewModelProvider().get(), DragTarget.Type.SCRIPTURE_TAKE) {

    private val recordScriptureViewModel: RecordScriptureViewModel by inject()

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
                //previous verse button
                button(messages["previousVerse"], AppStyles.backIcon()) {
                    addClass(RecordScriptureStyles.navigationButton)
                    action {
                        recordScriptureViewModel.previousChunk()
                    }
                    enableWhen(recordScriptureViewModel.hasPrevious)
                }
                add(dragTarget)

                //next verse button
                button(messages["nextVerse"], AppStyles.forwardIcon()) {
                    addClass(RecordScriptureStyles.navigationButton)
                    contentDisplay = ContentDisplay.RIGHT
                    action {
                        recordScriptureViewModel.nextChunk()
                        enableWhen(recordScriptureViewModel.hasNext)
                    }
                }
            }

            // Add the available takes flow pane
            scrollpane {
                vgrow = Priority.ALWAYS
                isFitToWidth = true
                addClass(RecordScriptureStyles.scrollpane)
                add(
                    TakesFlowPane(
                        recordableViewModel.alternateTakes,
                        audioPluginViewModel::audioPlayer,
                        lastPlayOrPauseEvent,
                        recordableViewModel::recordNewTake
                    )
                )
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
