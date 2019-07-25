package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import com.jfoenix.controls.JFXSnackbar
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordScriptureViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.progressdialog
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.*
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.DeleteTakeEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.EditTakeEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.PlayOrPauseEvent
import tornadofx.*

class RecordScriptureFragment : DragTakeFragment() {
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val recordScriptureViewModel: RecordScriptureViewModel by inject()
    override val recordableViewModel = recordScriptureViewModel.recordableViewModel

    private val lastPlayOrPauseEvent: SimpleObjectProperty<PlayOrPauseEvent?> = SimpleObjectProperty()

    init {
        importStylesheet<RecordScriptureStyles>()
        importStylesheet<TakeCardStyles>()

        root.apply {
            addEventHandler(PlayOrPauseEvent.PLAY) {
                lastPlayOrPauseEvent.set(it)
            }
            addEventHandler(DeleteTakeEvent.DELETE_TAKE) {
                recordableViewModel.deleteTake(it.take)
            }
            addEventHandler(EditTakeEvent.EDIT_TAKE) {
                recordableViewModel.editTake(it)
            }

            addClass(RecordScriptureStyles.background)
            val snackBar = JFXSnackbar(this as AnchorPane) // TODO
            recordableViewModel.snackBarObservable.subscribe { shouldShow ->
                snackBar.enqueue(
                    JFXSnackbar.SnackbarEvent(
                        messages["noRecorder"],
                        messages["addPlugin"].toUpperCase(),
                        5000,
                        false,
                        EventHandler {
                            audioPluginViewModel.addPlugin(true, false)
                        })
                )
            }
        }

        mainContainer.apply {
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
                //selected take and drag target
                stackpane {
                    // drag target glow
                    add(dragComponents
                        .dragTargetBottom {
                            addClass(RecordScriptureStyles.dragTarget, RecordScriptureStyles.glow)
                        })

                    add(dragComponents
                        .selectedTakeContainer {
                            addClass(TakeCardStyles.scriptureTakeCardPlaceholder)
                            vgrow = Priority.NEVER
                        })

                    add(dragComponents
                        .dragTargetTop {
                            addClass(RecordScriptureStyles.dragTarget)
                            alignment = Pos.CENTER
                            add(MaterialIconView(MaterialIcon.ADD, "30px"))
                        })
                }
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
                        recordableViewModel.lastPlayOrPauseEvent,
                        recordableViewModel::recordNewTake
                    )
                )
            }
        }

        // Plugin active cover
        val dialog = progressdialog {
            root.addClass(AppStyles.progressDialog)
            recordableViewModel.contextProperty.toObservable().subscribe { newContext ->
                when (newContext) {
                    TakeContext.RECORD -> graphic = AppStyles.recordIcon("60px")
                    TakeContext.EDIT_TAKES -> graphic = AppStyles.editIcon("60px")
                }
            }
        }
        recordableViewModel.showPluginActiveProperty.onChange {
            Platform.runLater {
                if (it == true) dialog.open() else dialog.close()
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
