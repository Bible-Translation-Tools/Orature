package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view

import com.github.thomasnield.rxkotlinfx.toObservable
import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.input.MouseEvent
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.viewmodel.ViewTakesViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.progressdialog
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.TakeCardStyles
import tornadofx.*

class ViewTakesView : View() {
    private val viewModel: ViewTakesViewModel by inject()

    // The currently selected take
    private var selectedTakeProperty = SimpleObjectProperty<TakeCard>()
    // Take at the top to compare to an existing selected take
    private var draggingTakeProperty = SimpleObjectProperty<TakeCard>()

    // Drag target to show when drag action in progress
    private var dragTarget: StackPane by singleAssign()

    // Drag shadow (node that actually moves with cursor)
    private var dragShadow: Node = VBox()

    // Record button?
    private var recordButton: Button by singleAssign()

    // Flow pane of available takes
    private var takesFlowPane = createTakesFlowPane()

    init {
        importStylesheet<ViewTakesStyles>()
    }

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
                        addClass(AppStyles.backButton)
                    })
                }

                // Top items above the alternate takes
                // Drag target and/or selected take
                stackpane {
                    alignment = Pos.CENTER_LEFT
                    addClass(ViewTakesStyles.headerContainer)

                    // drag target glow
                    stackpane {
                        addClass(ViewTakesStyles.dragTarget, ViewTakesStyles.glow)
                        visibleProperty().bind(draggingTakeProperty.booleanBinding { it != null })
                    }
                    vbox {
                        // Check if the selected take card has changed
                        isFillWidth = false
                        val placeholder = vbox {
                            addClass(ViewTakesStyles.placeholder)
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
                        addClass(ViewTakesStyles.dragTarget)
                        add(MaterialIconView(MaterialIcon.ADD, "30px"))
                        // Initially hide the drag target
                        visibleProperty().bind(draggingTakeProperty.booleanBinding { it != null })
                    }
                }

                // Add the available takes flow pane
                add(takesFlowPane)
            }

            // Record button?
            recordButton = button("", ViewTakesStyles.recordIcon("25px")) {
                addClass(ViewTakesStyles.recordButton)
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
            graphic = ViewTakesStyles.recordIcon("60px")
        }
        viewModel.showPluginActiveProperty.onChange {
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
            val widthOffset = 116
            val heightOffset = 120 / 2
            dragShadow.toFront()
            dragShadow.relocate(evt.sceneX - widthOffset, evt.sceneY - heightOffset)
        }
    }

    private fun cancelDrag(evt: MouseEvent) {
        takesFlowPane.add(draggingTakeProperty.value)
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
            addClass(ViewTakesStyles.takeFlowPane)
            // Update the takes displayed
            viewModel.alternateTakes.onChange {
                clear()
                it.list.forEach { take ->
                    // Add a new take card
                    add(createTakeCard(take))
                }
                sortTakesFlowPane(takesFlowPane)
            }
        }
    }

    private fun sortTakesFlowPane(flowPane: FlowPane) {
        flowPane.children.setAll(flowPane.children.sortedBy { (it as TakeCard).take.number })
    }

    private fun createTakeCard(take: Take): TakeCard {
        return TakeCard(take, Injector.audioPlayer).apply {
            addClass(ViewTakesStyles.takeCard)
            badge.addClass(ViewTakesStyles.badge)
            simpleAudioPlayer.playPauseButton.addClass(ViewTakesStyles.playPauseButton)
            playedProperty.onChange {
                if (it) viewModel.setTakePlayed(take)
            }
            deleteButton.apply {
                addClass(ViewTakesStyles.deleteButton)
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
        super.onDock()
        // Reset the model
        viewModel.reset()
    }
}