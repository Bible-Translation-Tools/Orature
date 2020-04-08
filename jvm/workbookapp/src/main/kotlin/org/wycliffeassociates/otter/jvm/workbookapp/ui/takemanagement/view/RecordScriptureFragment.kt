package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceAudioSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCardStyles
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.scripturetakecard
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.RecordScriptureViewModel
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

    private val takesGrid = ScriptureTakesGridView(recordableViewModel::recordNewTake)

    private val sourceAudioPlayer =
        AudioPlayerNode(null).apply {
            style {
                skin = SourceAudioSkin::class
            }
        }

    init {
        importStylesheet<RecordScriptureStyles>()
        importStylesheet<TakeCardStyles>()

        importStylesheet(javaClass.getResource("/css/scripturetakecard.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/audioplayer.css").toExternalForm())

        sourceAudioPlayer.visibleWhen { recordScriptureViewModel.sourceAudioAvailableProperty }
        sourceAudioPlayer.managedWhen { sourceAudioPlayer.visibleProperty() }
        recordScriptureViewModel.sourceAudioPlayerProperty.onChangeAndDoNow {
            it?.let {
                sourceAudioPlayer.pause()
                sourceAudioPlayer.load(it)
            }
        }

        recordableViewModel.takeModels.onChangeAndDoNow {
            takesGrid.gridItems.setAll(it)
        }

        dragTarget.setOnDragDropped {
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

        dragTarget.setOnDragOver {
            if (it.gestureSource != dragTarget && it.dragboard.hasString()) {
                it.acceptTransferModes(*TransferMode.ANY)
            }
            it.consume()
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
                        recordScriptureViewModel.nextChunk()
                        enableWhen(recordScriptureViewModel.hasNext)
                    }
                }
            }
            add(takesGrid)
            add(sourceAudioPlayer)
        }
    }

    override fun closePlayers() {
        // takesGrid.closePlayers()
    }

    override fun createTakeCard(take: Take): TakeCard {
        return scripturetakecard(
            take,
            audioPluginViewModel.audioPlayer(),
            lastPlayOrPauseEvent.toObservable()
        )
    }
}