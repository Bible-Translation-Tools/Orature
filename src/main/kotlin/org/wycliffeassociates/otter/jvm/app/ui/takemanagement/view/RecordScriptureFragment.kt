package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import com.jfoenix.controls.JFXSnackbar
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.ContentDisplay
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

    // The currently selected take
    private var selectedTakeCardProperty = SimpleObjectProperty<TakeCard>()

    init {
        importStylesheet<RecordScriptureStyles>()
    }

    override fun getDragTargetBuilder() = DragTargetBuilder(
        stackpane {
            addClass(RecordScriptureStyles.dragTarget)
            add(MaterialIconView(MaterialIcon.ADD, "30px"))
        }
    )

    override val root: Parent = anchorpane {

        addDragTakeEventHandlers()

        addEventHandler(PlayOrPauseEvent.PLAY) {
            lastPlayOrPauseEvent.set(it)
        }
        addEventHandler(DeleteTakeEvent.DELETE_TAKE) {
            recordableViewModel.deleteTake(it.take)
        }
        addEventHandler(EditTakeEvent.EDIT_TAKE) {
            recordableViewModel.editTake(it)
        }

        anchorpaneConstraints {
            leftAnchor = 0.0
            rightAnchor = 0.0
            bottomAnchor = 0.0
            topAnchor = 0.0
        }
        addClass(AppStyles.appBackground)
        addClass(RecordScriptureStyles.tpanelStyle)
        val snackBar = JFXSnackbar(this)
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
        vbox {
            anchorpaneConstraints {
                leftAnchor = 0.0
                rightAnchor = 0.0
                bottomAnchor = 0.0
                topAnchor = 0.0
            }

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
                    addClass(RecordScriptureStyles.selectedTakeContainer)
                    // drag target glow
                    stackpane {
                        addClass(RecordScriptureStyles.dragTarget, RecordScriptureStyles.glow)
                        visibleProperty().bind(draggingNodeProperty.booleanBinding { it != null })
                    }
                    vbox {
                        alignment = Pos.CENTER

                        // Check if the selected take card has changed
                        isFillWidth = false
                        val placeholder = vbox {
                            addClass(RecordScriptureStyles.placeholder)
                            vgrow = Priority.NEVER
                        }

                        // Listen for changes when the drag and drop occurs
                        selectedTakeCardProperty.onChange {
                            clear()
                            if (it == null) {
                                // No currently selected take
                                add(placeholder)
                            } else {
                                add(it)
                            }
                        }
                        recordableViewModel.selectedTakeProperty.onChange {
                            // The view model wants us to use this selected take
                            // This take will not appear in the flow pane items
                            when (it) {
                                null -> selectedTakeCardProperty.value = null
                                else -> selectedTakeCardProperty.value = createTakeCard(it)
                            }
                        }
                    }

                    add(dragTarget())
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
                add(TakesFlowPane(
                    recordableViewModel.alternateTakes,
                    audioPluginViewModel::audioPlayer,
                    recordableViewModel.lastPlayOrPauseEvent,
                    recordableViewModel::recordNewTake
                ))
            }
        }

        add(dragContainer)

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

    private fun createTakeCard(take: Take): TakeCard {
        return scripturetakecard(
            take,
            audioPluginViewModel.audioPlayer(),
            lastPlayOrPauseEvent.toObservable()
        )
    }
}
