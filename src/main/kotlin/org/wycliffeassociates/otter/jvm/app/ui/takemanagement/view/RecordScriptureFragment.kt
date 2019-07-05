package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import com.jfoenix.controls.JFXSnackbar
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ButtonType
import javafx.scene.control.ContentDisplay
import javafx.scene.input.MouseEvent
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.TakeManagementViewModel
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.progressdialog
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.OldTakeCard
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.oldtakecard
import tornadofx.*

class RecordScriptureFragment : Fragment() {
    private val recordScriptureViewModel: RecordScriptureViewModel by inject()
    private val workbookViewModel: WorkbookViewModel by inject()

    // The currently selected take
    private var selectedTakeProperty = SimpleObjectProperty<OldTakeCard>()
    // Take at the top to compare to an existing selected take
    private var draggingTakeProperty = SimpleObjectProperty<OldTakeCard>()

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

        anchorpaneConstraints {
            leftAnchor = 0.0
            rightAnchor = 0.0
            bottomAnchor = 0.0
            topAnchor = 0.0
        }
        addClass(AppStyles.appBackground)
        addClass(RecordScriptureStyles.tpanelStyle)
        val snackBar = JFXSnackbar(this)
        takeManagementViewModel.snackBarObservable.subscribe { shouldShow ->
            snackBar.enqueue(
                    JFXSnackbar.SnackbarEvent(messages["noRecorder"], messages["addPlugin"].toUpperCase(), 5000, false, EventHandler {
                        takeManagementViewModel.addPlugin(true, false)
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
                        recordScriptureViewModel.previousVerse()
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
                        selectedTakeProperty.onChange {
                            clear()
                            if (it == null) {
                                // No currently selected take
                                add(placeholder)
                            } else {
                                // Add the selected take card
                                recordScriptureViewModel.acceptTake(it.take)
                                add(it)
                            }
                        }
                        takeManagementViewModel.selectedTakeProperty.onChange {
                            // The view model wants us to use this selected take
                            // This take will not appear in the flow pane items
                            if (it != null && selectedTakeProperty.value == null) {
                                selectedTakeProperty.value = createTakeCard(it)
                            } else if (it == null) selectedTakeProperty.value = null
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
                        recordScriptureViewModel.nextVerse()
                        enableWhen(takeManagementViewModel.hasNext)
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
            recordScriptureViewModel.contextProperty.toObservable().subscribe { newContext ->
                when (newContext) {
                    TakeContext.RECORD -> graphic = AppStyles.recordIcon("60px")
                    TakeContext.EDIT_TAKES -> graphic = AppStyles.editIcon("60px")
                }
            }
        }
        recordScriptureViewModel.showPluginActiveProperty.onChange {
            Platform.runLater {
                if (it == true) dialog.open() else dialog.close()
            }
        }
    }

    private fun startDrag(evt: MouseEvent) {
        // Get the take being dragged
        val target = evt.target as Node

        // Remove from the flow pane
        val takeCard = target.findParentOfType(OldTakeCard::class) as OldTakeCard
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
            selectedTakeProperty.value = draggingTakeProperty.value
            draggingTakeProperty.value = null
        } else cancelDrag(evt)
    }

    // Create the flow pane of alternate takes
    private fun createTakesFlowPane(): FlowPane {
        return FlowPane().apply {
            vgrow = Priority.ALWAYS
            addClass(RecordScriptureStyles.takeGrid)
            // Update the takes displayed
            recordScriptureViewModel.alternateTakes.onChange {
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
                    recordScriptureViewModel.recordContent(workbookViewModel.chunk!!)
                }
            }
        }
    }

    private fun sortTakesFlowPane(flowPane: FlowPane) {
        flowPane.children.setAll(flowPane.children.sortedBy {
            (it as OldTakeCard).take.number
        })
        //add the newTakeCard here after we have sorted all other takes by take number
        flowPane.children.add(0, createRecordCard())
    }

    private fun createTakeCard(take: Take): OldTakeCard {
        return oldtakecard(take, takeManagementViewModel.audioPlayer(), messages["take"]) {
            addClass(RecordScriptureStyles.takeCard)
            playedProperty.onChange {
                if (it) takeManagementViewModel.setTakePlayed(take)
            }
            deleteButton.apply {
                addClass(RecordScriptureStyles.deleteButton)
                action {
                    error(
                            messages["deleteTakePrompt"],
                            messages["cannotBeUndone"],
                            ButtonType.YES,
                            ButtonType.NO,
                            title = messages["deleteTakePrompt"]
                    ) { button: ButtonType ->
                        if (button == ButtonType.YES) takeManagementViewModel.delete(take)
                    }
                }
            }
            editButton.action {
                takeManagementViewModel.editContent(take)
            }
            addEventHandler(MouseEvent.MOUSE_PRESSED, ::startDrag)
        }
    }

    override fun onDock() {
        super.onDock()
        // Reset the model
        recordScriptureViewModel.reset()
    }
}