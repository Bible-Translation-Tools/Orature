package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Control
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.controls.sourcecontent.SourceContent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCardStyles
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.RecordScriptureViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
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
    private val workbookViewModel: WorkbookViewModel by inject()

    private val takesGrid = ScriptureTakesGridView(recordableViewModel::recordNewTake)

    private val sourceContent =
        SourceContent().apply {
            sourceTextProperty.bind(workbookViewModel.sourceTextBinding())
            audioPlayerProperty.bind(recordableViewModel.sourceAudioPlayerProperty)

            audioNotAvailableTextProperty.set(messages["audioNotAvailable"])
            textNotAvailableTextProperty.set(messages["textNotAvailable"])
            playLabelProperty.set(messages["playSource"])
            pauseLabelProperty.set(messages["pauseSource"])

            contentTitleProperty.bind(workbookViewModel.activeChunkTitleBinding())
        }

    init {
        importStylesheet<RecordScriptureStyles>()
        importStylesheet<TakeCardStyles>()
        importStylesheet(javaClass.getResource("/css/scripturetakecard.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/audioplayer.css").toExternalForm())

        recordableViewModel.takeCardModels.onChangeAndDoNow {
            takesGrid.gridItems.setAll(it)
        }

        recordableViewModel.selectedTakeProperty.onChangeAndDoNow {
            if (it != null) {
                dragTarget.selectedNodeProperty.set(createTakeCard(it))
            }
        }

        mainContainer.apply {
            addClass(RecordScriptureStyles.background)

            hgrow = Priority.ALWAYS
            // Top items above the alternate takes
            // Drag target and/or selected take, Next Verse Button, Previous Verse Button
            hbox {
                addClass(RecordScriptureStyles.pageTop)
                alignment = Pos.CENTER
                // previous verse button
                button(messages["previousVerse"], AppStyles.backIcon()) {
                    addClass(RecordScriptureStyles.navigationButton)
                    action {
                        closePlayers()
                        recordScriptureViewModel.previousChunk()
                    }
                    enableWhen(recordScriptureViewModel.hasPrevious)
                }
                vbox {
                    add(dragTarget)
                }

                // next verse button
                button(messages["nextVerse"], AppStyles.forwardIcon()) {
                    addClass(RecordScriptureStyles.navigationButton)
                    contentDisplay = ContentDisplay.RIGHT
                    action {
                        closePlayers()
                        recordScriptureViewModel.nextChunk()
                    }
                    enableWhen(recordScriptureViewModel.hasNext)
                }
            }
            add(takesGrid)
            add(sourceContent)
        }
    }

    override fun closePlayers() {
        recordableViewModel.takeCardModels.forEach { it.audioPlayer.close() }
    }

    override fun openPlayers() {
        (dragTarget.selectedNodeProperty.get() as? TakeCard)?.simpleAudioPlayer?.refresh()
        takesGrid.reloadPlayers()
    }

    override fun createTakeCard(take: TakeCardModel): Control {
        val card = ScriptureTakeCard().apply {
            audioPlayerProperty().set(take.audioPlayer)
            this.deleteTextProperty().set(take.deleteText)
            this.editTextProperty().set(take.editText)
            this.pauseTextProperty().set(take.playText)
            this.playTextProperty().set(take.playText)
            this.takeProperty().set(take.take)
            this.takeNumberProperty().set(take.take.number.toString())
        }
        return card
    }
}
