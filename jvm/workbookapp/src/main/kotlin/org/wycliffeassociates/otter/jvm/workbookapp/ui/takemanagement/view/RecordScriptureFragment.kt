package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.input.DragEvent
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.controls.sourcecontent.SourceContent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCardStyles
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.scripturetakecard
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
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

    private val isDraggingProperty = SimpleBooleanProperty(false)

    private val scriptureDragTarget =
        DragTargetBuilder(DragTargetBuilder.Type.SCRIPTURE_TAKE)
            .build(isDraggingProperty.toBinding())
            .apply {
                recordableViewModel.selectedTakeProperty.onChangeAndDoNow { take ->
                    /* We can't just add the node being dragged, since the selected take might have just been
                        loaded from the database */
                    this.selectedNodeProperty.value = take?.let { createTakeCard(take) }
                }
            }

    private val sourceContent =
        SourceContent().apply {
            visibleWhen(recordableViewModel.sourceAudioAvailableProperty)
            managedWhen(visibleProperty())

            sourceAudioLabelProperty.set(messages["sourceAudio"])
            sourceTextLabelProperty.set(messages["sourceText"])

            recordableViewModel.recordableProperty.onChangeAndDoNow {
                it?.let {
                    sourceTextProperty.set(workbookViewModel.getSourceText().blockingGet())
                }
            }
            audioPlayerProperty.bind(recordableViewModel.sourceAudioPlayerProperty)
        }

    init {
        importStylesheet<RecordScriptureStyles>()
        importStylesheet<TakeCardStyles>()
        importStylesheet(javaClass.getResource("/css/scripturetakecard.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/audioplayer.css").toExternalForm())

        recordableViewModel.takeCardModels.onChangeAndDoNow {
            takesGrid.gridItems.setAll(it)
        }

        scriptureDragTarget.setOnDragDropped {
            val db: Dragboard = it.dragboard
            var success = false
            if (db.hasString()) {
                recordableViewModel.selectTake(db.string)
                success = true
            }
            (it.source as? ScriptureTakeCard)?.let {
                it.isDraggingProperty().value = false
            }
            it.setDropCompleted(success)
            it.consume()
        }

        scriptureDragTarget.setOnDragOver {
            if (it.gestureSource != scriptureDragTarget && it.dragboard.hasString()) {
                it.acceptTransferModes(*TransferMode.ANY)
            }
            it.consume()
        }

        mainContainer.apply {

            addEventHandler(DragEvent.DRAG_ENTERED, { isDraggingProperty.value = true })
            addEventHandler(DragEvent.DRAG_EXITED, { isDraggingProperty.value = false })

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
                    add(scriptureDragTarget)
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
        takesGrid.closePlayers()
    }

    override fun createTakeCard(take: Take): TakeCard {
        return scripturetakecard(
            take,
            audioPluginViewModel.audioPlayer(),
            lastPlayOrPauseEvent.toObservable()
        )
    }
}
