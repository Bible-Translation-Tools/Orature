package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordableViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.AnimateDragEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.CompleteDragEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.StartDragEvent
import tornadofx.*

abstract class DragTakeFragment : Fragment() {

    inner class DragTargetBuilder(private val node: Node) {
        fun build() = node.apply {
            visibleProperty().bind(draggingNodeProperty.booleanBinding { it != null })
            dragTarget = this
        }
    }
    abstract fun getDragTargetBuilder(): DragTargetBuilder
    fun dragTarget(): Node = getDragTargetBuilder().build()
    var dragTarget: Node by singleAssign()


    abstract val recordableViewModel: RecordableViewModel

    // Drag Container (node that actually moves with cursor)
//    var dragContainer: Node by singleAssign()

    val draggingNodeProperty = SimpleObjectProperty<Node>()

    val dragContainer = createDragContainer()

    private fun createDragContainer() = VBox().apply {
        draggingNodeProperty.onChange {
            clear()
            if (it != null) {
                add(it)
                show()
            } else {
                hide()
            }
        }
        hide()
    }

    fun Parent.addDragTakeEventHandlers() {
        addEventHandler(StartDragEvent.START_DRAG, ::startDrag)
        addEventHandler(AnimateDragEvent.ANIMATE_DRAG, ::animateDrag)
        addEventHandler(CompleteDragEvent.COMPLETE_DRAG, ::completeDrag)
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
        if (dragTarget.contains(dragTarget.sceneToLocal(evt.mouseEvent.sceneX, evt.mouseEvent.sceneY))) {
            onDraggedToTarget(evt.take)
            draggingNodeProperty.set(null)
        } else cancelDrag(evt.onCancel)
    }

    private fun cancelDrag(onCancel: () -> Unit) {
        draggingNodeProperty.set(null)
        onCancel()
    }
}