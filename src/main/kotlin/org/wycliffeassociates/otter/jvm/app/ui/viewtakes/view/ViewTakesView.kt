package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.animation.Interpolator
import javafx.animation.Timeline
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.util.Duration
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.view.ChapterContext
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.viewmodel.ViewTakesViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.TakeCard
import tornadofx.*

class ViewTakesView : View() {
    val viewModel: ViewTakesViewModel by inject()

    // The currently selected take
    var selectedTakeProperty = SimpleObjectProperty<TakeCard>()
    // Take at the top to compare to an existing selected take
    var proposedTakeProperty = SimpleObjectProperty<TakeCard>()
    var draggingTakeProperty = SimpleObjectProperty<TakeCard>()
    var proposedTakeContainer = VBox()

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
                hbox {
                    addClass(ViewTakesStylesheet.headerContainer)
                    // Create the drag target
                    dragTarget = stackpane {
                        addClass(ViewTakesStylesheet.dragTarget)
                        label("Drag Here")
                        // Initially hide the drag target
                        hide()
                        draggingTakeProperty.onChange {
                            if (it == null) {
                                // Nothing to drag
                                hide()
                            } else {
                                // Something is being dragged
                                show()
                            }
                        }
                    }

                    // Container for proposed take and buttons
                    proposedTakeContainer = vbox(10.0) {
                        proposedTakeProperty.onChange {
                            if (it != null) {
                                add(it)
                                // Create the accept/reject buttons
                                var actionButtons = HBox()
                                actionButtons = hbox(10.0) {
                                    addClass(ViewTakesStylesheet.actionButtonsContainer)
                                    button("", MaterialIconView(MaterialIcon.CLOSE)) {
                                        addClass(ViewTakesStylesheet.rejectButton)
                                        action {
                                            actionButtons.removeFromParent()
                                            // Add back to the flow pane
                                            takesFlowPane.add(it)
                                            sortTakesFlowPane()
                                            proposedTakeProperty.value = null
                                        }
                                    }
                                    button("", MaterialIconView(MaterialIcon.CHECK)) {
                                        addClass(ViewTakesStylesheet.acceptButton)
                                        action {
                                            actionButtons.removeFromParent()
                                            // Move the old selected take back to the flow pane
                                            if (selectedTakeProperty.value != null) {
                                                takesFlowPane.add(selectedTakeProperty.value)
                                                sortTakesFlowPane()
                                            }
                                            // Put in the new selected take
                                            selectedTakeProperty.value = it
                                            proposedTakeProperty.value = null
                                        }
                                    }
                                }
                            } else {
                                // No more proposed take
                                clear()
                            }
                        }
                    }

                    // Container to show arrows
                    hbox {
                        addClass(ViewTakesStylesheet.arrowContainer)
                        for (i in 0..2) {
                            val arrow = MaterialIconView(MaterialIcon.PLAY_ARROW, "25px")
                            arrow.opacity = 0.0
                            timeline {
                                keyframe(Duration.seconds(i * 0.5)) {
                                    keyvalue(arrow.opacityProperty(), 0.0, Interpolator.LINEAR)
                                }
                                keyframe(Duration.seconds(0.5 * (i + 1))) {
                                    keyvalue(arrow.opacityProperty(), 1.0, Interpolator.LINEAR)
                                }
                                keyframe(1.5.seconds) {
                                    keyvalue(arrow.opacityProperty(), 1.0, Interpolator.LINEAR)
                                }
                                keyframe(2.seconds) {
                                    keyvalue(arrow.opacityProperty(), 0.0, Interpolator.LINEAR)
                                }
                                cycleCount = Timeline.INDEFINITE
                            }
                            add(arrow)
                        }
                        hiddenWhen(draggingTakeProperty.isNull.and(proposedTakeProperty.isNull))
                    }

                    // Does a selected take exist?
                    vbox {
                        // Check if the selected take card has changed
                        val placeholder = vbox {
                            addClass(ViewTakesStylesheet.placeholder)
                            vgrow = Priority.NEVER
                        }

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
                }

                // Add the available takes flow pane
                add(takesFlowPane)
            }

            // Record button?
            val recordIcon = MaterialIconView(MaterialIcon.MIC_NONE, "25px")
            recordButton = button("", recordIcon) {
                anchorpaneConstraints {
                    bottomAnchor = 25.0
                    rightAnchor = 25.0
                }
                style {
                    backgroundColor += c(Colors["base"])
                    recordIcon.fill = c(Colors["primary"])
                    backgroundRadius += box(100.0.px)
                    borderRadius += box(100.0.px)
                    prefHeight = 50.px
                    prefWidth = 50.px
                    effect = DropShadow(10.0, Color.GRAY)
                }

                action {
                    viewModel.recordTake()
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

        stackpane {
            style {
                alignment = Pos.CENTER
                backgroundColor += Color.BLACK
                        .deriveColor(0.0, 0.0, 0.0, 0.5)
            }
            val icon = MaterialIconView(MaterialIcon.MIC_NONE, "60px")
                    .apply {
                        style(true) {
                            fill = Color.WHITE
                        }
                    }
            add(icon)
            progressindicator {
                style {
                    maxWidth = 125.px
                    maxHeight = 125.px
                    progressColor = Color.WHITE
                }
            }
            visibleProperty().bind(viewModel.showPluginActiveProperty)
        }
    }

    private fun startDrag(evt: MouseEvent) {
        // Get the take being dragged
        val target = evt.target as Node

        // Remove from the flow pane
        val takeCard = target.findParentOfType(TakeCard::class) as TakeCard
        if (takeCard.parent == takesFlowPane && proposedTakeProperty.value == null) {
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
        if (dragTarget.contains(dragTarget.sceneToLocal(evt.sceneX, evt.sceneY)) && proposedTakeProperty.value == null) {
            proposedTakeProperty.value = draggingTakeProperty.value
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
                it.list.forEach {
                    // Add a new take card
                    add(createTakeCard(it))
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
            addEventHandler(MouseEvent.MOUSE_PRESSED, ::startDrag)
        }
    }

    override fun onDock() {
        // Reset the model
        viewModel.reset()
    }

    override fun onUndock() {
        proposedTakeProperty.value = null
    }
}