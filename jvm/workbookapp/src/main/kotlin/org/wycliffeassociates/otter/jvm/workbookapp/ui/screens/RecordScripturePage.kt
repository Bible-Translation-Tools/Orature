package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Control
import javafx.scene.input.DragEvent
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.RowConstraints
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCardStyles
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordScriptureViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.RecordScriptureStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

private class RecordScriptureViewModelProvider : Component() {
    private val recordScriptureViewModel: RecordScriptureViewModel by inject()
    fun get() = recordScriptureViewModel.recordableViewModel
}

private const val TAKES_ROW_HEIGHT = 170.0

class RecordScriptureFragment : RecordableFragment(
    RecordScriptureViewModelProvider().get(),
    DragTargetBuilder(DragTargetBuilder.Type.SCRIPTURE_TAKE)
) {
    private val recordScriptureViewModel: RecordScriptureViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()

    private val takesGrid = ScriptureTakesGridView(
        workbookDataStore.activeChunkProperty.isNull,
        recordableViewModel::recordNewTake
    )

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
            vgrow = Priority.ALWAYS

            sourceTextProperty.bind(workbookDataStore.sourceTextBinding())
            audioPlayerProperty.bind(recordableViewModel.sourceAudioPlayerProperty)

            audioNotAvailableTextProperty.set(messages["audioNotAvailable"])
            textNotAvailableTextProperty.set(messages["textNotAvailable"])
            playLabelProperty.set(messages["playSource"])
            pauseLabelProperty.set(messages["pauseSource"])

            contentTitleProperty.bind(workbookDataStore.activeChunkTitleBinding())
        }

    init {
        importStylesheet<RecordScriptureStyles>()
        importStylesheet<TakeCardStyles>()
        importStylesheet(javaClass.getResource("/css/scripturetakecard.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/audioplayer.css").toExternalForm())

        isDraggingProperty.onChange {
            if (it) stopPlayers()
        }

        recordableViewModel.takeCardModels.onChangeAndDoNow {
            takesGrid.gridItems.setAll(it)
        }

        recordableViewModel.selectedTakeProperty.onChangeAndDoNow {
            if (it != null) {
                dragTarget.selectedNodeProperty.set(createTakeCard(it))
            }
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

            gridpane {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS

                add(takesGrid, 0, 0)
                add(sourceContent, 0, 1)

                columnConstraints.addAll(
                    ColumnConstraints(
                        0.0,
                        0.0,
                        Double.MAX_VALUE,
                        Priority.ALWAYS,
                        HPos.LEFT,
                        true
                    )
                )

                val takesRowConstraints = RowConstraints(
                    TAKES_ROW_HEIGHT,
                    TAKES_ROW_HEIGHT,
                    Double.MAX_VALUE,
                    Priority.ALWAYS,
                    VPos.CENTER,
                    true
                )

                val sourceContentRowConstraints = RowConstraints(
                    Region.USE_COMPUTED_SIZE,
                    Region.USE_COMPUTED_SIZE,
                    Region.USE_COMPUTED_SIZE,
                    Priority.NEVER,
                    VPos.CENTER,
                    false
                )

                rowConstraints.add(takesRowConstraints)
                rowConstraints.add(sourceContentRowConstraints)
            }
        }
    }

    override fun stopPlayers() {
        recordableViewModel.stopPlayers()
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
            this.markerTextProperty().set(take.markerText)
            this.takeProperty().set(take.take)
            this.takeNumberProperty().set(take.take.number.toString())
            this.allowMarkerProperty().bind(recordableViewModel.workbookDataStore.activeChunkProperty.isNull)
        }
        return card
    }
}
