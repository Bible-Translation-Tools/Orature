package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceAudioSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.RecordScriptureViewModel
import org.wycliffeassociates.otter.jvm.controls.dragtarget.DragTargetBuilder
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

    private val takesList = ScriptureTakesGridView(recordableViewModel::recordNewTake)

    private val sourceAudioPlayer =
        AudioPlayerNode(null).apply {
            style {
                skin = SourceAudioSkin::class
            }
        }

    init {
        importStylesheet<RecordScriptureStyles>()
        importStylesheet<TakeCardStyles>()
        importStylesheet(javaClass.getResource("/css/audioplayer.css").toExternalForm())

        sourceAudioPlayer.visibleWhen { recordScriptureViewModel.sourceAudioAvailableProperty }
        sourceAudioPlayer.managedWhen { sourceAudioPlayer.visibleProperty() }
        recordScriptureViewModel.sourceAudioPlayerProperty.onChangeAndDoNow {
            it?.let {
                sourceAudioPlayer.pause()
                sourceAudioPlayer.load(it)
            }
        }

        takesList.gridItems.set(recordableViewModel.takeModels)

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
            add(takesList)
            add(sourceAudioPlayer)
        }
    }

    override fun closePlayers() {
        // takesList.closePlayers()
    }

    override fun createTakeCard(take: Take): TakeCard {
        return scripturetakecard(
            take,
            audioPluginViewModel.audioPlayer(),
            lastPlayOrPauseEvent.toObservable()
        )
    }
}