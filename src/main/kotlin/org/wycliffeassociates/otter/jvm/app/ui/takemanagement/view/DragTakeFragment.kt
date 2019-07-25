package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordableViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.AnimateDragEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.CompleteDragEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.StartDragEvent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

abstract class DragTakeFragment : Fragment() {

    // Can only use this in functions; otherwise, it will not be initialized yet
    abstract val recordableViewModel: RecordableViewModel

    abstract fun createTakeCard(take: Take): TakeCard

    /** Add custom components to this container, rather than root*/
    val mainContainer = VBox()

    private val draggingNodeProperty = SimpleObjectProperty<Node>()

    // This inner class better organizes the components that need to be added to the derived class
    /** Use the provided functions to add these components to your derived class */
    inner class DragComponents {
        private val dragTargetBottom = VBox().apply {
            bindVisibleToDraggingNodeProperty()
        }

        fun dragTargetBottom(runOnNode: (VBox.() -> Unit)? = null): Node = dragTargetBottom.apply {
            runOnNode?.let { it() }
        }

        private val dragTargetTop = VBox().apply {
            bindVisibleToDraggingNodeProperty()
        }

        fun dragTargetTop(runOnNode: (VBox.() -> Unit)? = null): Node = dragTargetTop.apply {
            runOnNode?.let { it() }
        }

        fun selectedTakeContainer(runOnPlaceHolder: Node.() -> Unit) = VBox().apply {
            alignment = Pos.CENTER

            val placeholder = vbox {
                runOnPlaceHolder()
            }

            recordableViewModel.selectedTakeProperty.onChangeAndDoNow {
                clear()
                when (it) {
                    null -> add(placeholder)
                    else -> add(createTakeCard(it))
                }
            }
        }
    }

    val dragComponents = DragComponents()
    private val dragTargetTop = dragComponents.dragTargetTop()

    private val dragContainer = VBox().apply {
        draggingNodeProperty.onChange {
            clear()
            it?.let { node -> add(node) }
        }
    }

    final override val root: Parent = anchorpane {
        addDragTakeEventHandlers()

        add(mainContainer.apply {
            anchorpaneConstraints {
                leftAnchor = 0.0
                rightAnchor = 0.0
                bottomAnchor = 0.0
                topAnchor = 0.0
            }
        })
        add(dragContainer)
    }

    private fun Parent.addDragTakeEventHandlers() {
        addEventHandler(StartDragEvent.START_DRAG, ::startDrag)
        addEventHandler(AnimateDragEvent.ANIMATE_DRAG, ::animateDrag)
        addEventHandler(CompleteDragEvent.COMPLETE_DRAG, ::completeDrag)
    }

    private fun Node.bindVisibleToDraggingNodeProperty() {
        visibleProperty().bind(draggingNodeProperty.booleanBinding { it != null })
    }

    private fun startDrag(evt: StartDragEvent) {
        if (evt.take != recordableViewModel.selectedTakeProperty.value) {
            draggingNodeProperty.set(evt.draggingNode)
            animateDrag(evt.mouseEvent)
        }
    }

    private fun animateDrag(evt: AnimateDragEvent) {
        animateDrag(evt.mouseEvent)
    }

    private fun animateDrag(evt: MouseEvent) {
        if (draggingNodeProperty.value != null) {
            val widthOffset = 348
            val heightOffset = 200
            dragContainer.toFront()
            dragContainer.relocate(evt.sceneX - widthOffset, evt.sceneY - heightOffset)
        }
    }

    private fun onDraggedToTarget(take: Take) {
        recordableViewModel.selectTake(take)
    }

    private fun completeDrag(evt: CompleteDragEvent) {
        if (dragTargetTop.contains(dragTargetTop.sceneToLocal(evt.mouseEvent.sceneX, evt.mouseEvent.sceneY))) {
            onDraggedToTarget(evt.take)
            draggingNodeProperty.set(null)
        } else cancelDrag(evt.onCancel)
    }

    private fun cancelDrag(onCancel: () -> Unit) {
        draggingNodeProperty.set(null)
        onCancel()
    }
}