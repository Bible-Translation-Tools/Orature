package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.animation.Interpolator
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.util.Duration
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.view.ChapterContext
import org.wycliffeassociates.otter.jvm.app.ui.styles.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.viewmodel.ViewTakesViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.TakeCard
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog
import tornadofx.*

class ViewTakesView : View() {
    val viewModel: ViewTakesViewModel by inject()

    // The currently selected take
    var selectedTakeProperty = SimpleObjectProperty<TakeCard>()
    // Take at the top to compare to an existing selected take
    var draggingTakeProperty = SimpleObjectProperty<TakeCard>()

    // Drag target to show when drag action in progress
    var dragTarget: StackPane by singleAssign()

    // Drag shadow (node that actually moves with cursor)
    var dragShadow: Node = VBox()

    // Record button?
    var recordButton: Button by singleAssign()

    // Flow pane of available takes
    private var takesFlowPane = createTakesFlowPane()

    override val root = stackpane {
        anchorpane {
            style {
                backgroundColor += c(Colors["base"])
            }
            vbox {
                anchorpaneConstraints {
                    leftAnchor = 0.0
                    rightAnchor = 0.0
                    topAnchor = 0.0
                    bottomAnchor = 0.0
                }
                hbox(20.0) {
                    style {
                        padding = box(20.px)
                    }
                    alignment = Pos.CENTER_LEFT
                    // Title label
                    label(viewModel.titleProperty) {
                        hgrow = Priority.ALWAYS
                        maxWidth = Double.MAX_VALUE
                        style {
                            fontSize = 40.px
                        }
                    }

                    // Back button
                    add(JFXButton(messages["back"], MaterialIconView(MaterialIcon.ARROW_BACK)).apply {
                        action {
                            workspace.navigateBack()
                        }
                        addClass(ViewTakesStylesheet.backButton)
                    })
                }

                // Top items above the alternate takes
                // Drag target and/or selected take
                stackpane {
                    alignment = Pos.CENTER_LEFT
                    addClass(ViewTakesStylesheet.headerContainer)

                    // drag target glow
                    stackpane {
                        addClass(ViewTakesStylesheet.dragTarget, ViewTakesStylesheet.glow)
                        visibleProperty().bind(draggingTakeProperty.booleanBinding { it != null })
                    }
                    vbox {
                        // Check if the selected take card has changed
                        isFillWidth = false
                        val placeholder = vbox {
                            addClass(ViewTakesStylesheet.placeholder)
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
                                viewModel.acceptTake(it.take)
                                add(it)
                            }
                        }

                        viewModel.selectedTakeProperty.onChange {
                            // The view model wants us to use this selected take
                            // This take will not appear in the flow pane items
                            if (it != null && selectedTakeProperty.value == null) {
                                selectedTakeProperty.value = createTakeCard(it)
                            } else if (it == null) selectedTakeProperty.value = null
                        }
                    }

                    // Create the drag target
                    dragTarget = stackpane {
                        addClass(ViewTakesStylesheet.dragTarget)
                        add(MaterialIconView(MaterialIcon.ADD, "30px"))
                        // Initially hide the drag target
                        visibleProperty().bind(draggingTakeProperty.booleanBinding { it != null })
                    }
                }

                // Add the available takes flow pane
                add(takesFlowPane)
            }

            // Record button?
            val recordIcon = MaterialIconView(MaterialIcon.MIC_NONE, "25px")
            recordButton = button("", recordIcon) {
                addClass(AppStyles.recordButton)
                anchorpaneConstraints {
                    bottomAnchor = 25.0
                    rightAnchor = 25.0
                }
                action {
                    viewModel.recordChunk()
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
        }

        // Plugin active cover
        val dialog = progressdialog {
            graphic = MaterialIconView(MaterialIcon.MIC_NONE, "60px")
        }
        viewModel.showPluginActiveProperty.onChange {
            Platform.runLater { if (it == true) dialog.open() else dialog.close() }
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
            val widthOffset = 116
            val heightOffset = 120 / 2
            dragShadow.toFront()
            dragShadow.relocate(evt.sceneX - widthOffset, evt.sceneY - heightOffset)
        }
    }

    private fun cancelDrag(evt: MouseEvent) {
        takesFlowPane.add(draggingTakeProperty.value)
        sortTakesFlowPane()
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
        val flowpane = FlowPane()
        flowpane.apply {
            vgap = 16.0
            hgap = 16.0
            vgrow = Priority.ALWAYS
            addClass(ViewTakesStylesheet.takeFlowPane)
            // Update the takes displayed
            viewModel.alternateTakes.onChange {
                clear()
                it.list.forEach { take ->
                    // Add a new take card
                    add(createTakeCard(take))
                }
                sortTakesFlowPane()
            }
        }
        return flowpane
    }

    private fun sortTakesFlowPane() {
        takesFlowPane.children.setAll(takesFlowPane.children.sortedBy { (it as TakeCard).take.number })
    }

    private fun createTakeCard(take: Take): TakeCard {
        return TakeCard(take, Injector.audioPlayer).apply {
            addClass(ViewTakesStylesheet.takeCard)
            playedProperty.onChange {
                if (it) {
                    // Take has been played
                    viewModel.setTakePlayed(take)
                }
            }
            deleteButton.apply {
                addClass(ViewTakesStylesheet.deleteButton)
                action {
                    error(
                            messages["deleteTakePrompt"],
                            messages["cannotBeUndone"],
                            ButtonType.YES,
                            ButtonType.NO,
                            title = messages["deleteTakePrompt"]
                    ) { button: ButtonType ->
                        if (button == ButtonType.YES) viewModel.delete(take)
                    }
                }
            }
            addEventHandler(MouseEvent.MOUSE_PRESSED, ::startDrag)
        }
    }

    override fun onDock() {
        // Reset the model
        viewModel.reset()
    }
}