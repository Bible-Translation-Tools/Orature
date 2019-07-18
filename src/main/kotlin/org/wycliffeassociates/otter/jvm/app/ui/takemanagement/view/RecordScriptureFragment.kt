package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import com.jfoenix.controls.JFXSnackbar
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ContentDisplay
import javafx.scene.input.MouseEvent
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.EditTake
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordScriptureViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.progressdialog
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.*
import tornadofx.*

class RecordScriptureFragment : Fragment() {
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val recordScriptureViewModel: RecordScriptureViewModel by inject()
    private val recordableViewModel = recordScriptureViewModel.recordableViewModel

    private val lastPlayOrPauseEvent: SimpleObjectProperty<PlayOrPauseEvent?> = SimpleObjectProperty()

    // The currently selected take
    private var selectedTakeCardProperty = SimpleObjectProperty<TakeCard>()
    // Take at the top to compare to an existing selected take
    private var draggingTakeProperty = SimpleObjectProperty<TakeCard>()

    // Drag target to show when drag action in progress
    private var dragTarget: StackPane by singleAssign()

    // Drag shadow (node that actually moves with cursor)
    private var dragShadow: Node = VBox()

    // Flow pane of available takes
    private var takesFlowPane = createTakesFlowPane()

    init {
        importStylesheet<RecordScriptureStyles>()
        takesFlowPane.children.add(createRecordCard())
    }

    override val root = anchorpane {

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
                        visibleProperty().bind(draggingTakeProperty.booleanBinding { it != null })
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

                    // Create the drag target
                    dragTarget = stackpane {
                        addClass(RecordScriptureStyles.dragTarget)
                        add(MaterialIconView(MaterialIcon.ADD, "30px"))
                        // Initially hide the drag target
                        visibleProperty().bind(draggingTakeProperty.booleanBinding { it != null })
                    }
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
                isFitToWidth = true
                addClass(RecordScriptureStyles.scrollpane)
                add(takesFlowPane)
            }
        }

        // Create drag shadow node and hide it initially
        dragShadow = vbox {
            draggingTakeProperty.onChange {
                clear()
                if (it != null) {
                    add(it)
                    show()
                } else {
                    hide()
                }
            }
            hide()
            addEventHandler(MouseEvent.MOUSE_DRAGGED, ::animateDrag)
            addEventHandler(MouseEvent.MOUSE_RELEASED, ::completeDrag)
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

    private fun startDrag(evt: MouseEvent) {
        // Get the take being dragged
        val target = evt.target as Node

        // Remove from the flow pane
        val takeCard = target.findParentOfType(TakeCard::class) as TakeCard
        if (takeCard.parent == takesFlowPane) {
            takeCard.removeFromParent()
            draggingTakeProperty.value = takeCard
        }
        animateDrag(evt)
    }

    private fun animateDrag(evt: MouseEvent) {
        if (draggingTakeProperty.value != null) {
            val widthOffset = 348
            val heightOffset = 200
            dragShadow.toFront()
            dragShadow.relocate(evt.sceneX - widthOffset, evt.sceneY - heightOffset)
        }
    }

    private fun cancelDrag(evt: MouseEvent) {
        takesFlowPane.add(draggingTakeProperty.value)
        //remove the new take card bc it isn't a take card and breaks sortTakesFlowPane
        takesFlowPane.children.removeAt(0)
        sortTakesFlowPane(takesFlowPane)
        draggingTakeProperty.value = null
    }

    private fun completeDrag(evt: MouseEvent) {
        if (dragTarget.contains(dragTarget.sceneToLocal(evt.sceneX, evt.sceneY))) {
            recordableViewModel.selectTake(draggingTakeProperty.value.take)
            draggingTakeProperty.value = null
        } else cancelDrag(evt)
    }

    // Create the flow pane of alternate takes
    private fun createTakesFlowPane(): FlowPane {
        return FlowPane().apply {
            vgrow = Priority.ALWAYS
            addClass(RecordScriptureStyles.takeGrid)
            // Update the takes displayed
            recordableViewModel.alternateTakes.onChange {
                clear()
                it.list.forEach { take ->
                    // Add a new take card
                    add(createTakeCard(take))
                }
                sortTakesFlowPane(takesFlowPane)
            }
        }
    }

    private fun createRecordCard(): VBox {
        return vbox(10.0) {
            alignment = Pos.CENTER
            addClass(RecordScriptureStyles.newTakeCard)
            label(messages["newTake"])
            button(messages["record"], AppStyles.recordIcon("25px")) {
                action {
                    recordableViewModel.recordNewTake()
                }
            }
        }
    }

    private fun sortTakesFlowPane(flowPane: FlowPane) {
        flowPane.children.setAll(flowPane.children.sortedBy {
            (it as TakeCard).take.number
        })
        //add the newTakeCard here after we have sorted all other takes by take number
        flowPane.children.add(0, createRecordCard())
    }

    private fun createTakeCard(take: Take): TakeCard {
        return scripturetakecard(take, audioPluginViewModel.audioPlayer(), lastPlayOrPauseEvent.toObservable()) {
            addEventHandler(MouseEvent.MOUSE_PRESSED, ::startDrag)
        }
    }
}