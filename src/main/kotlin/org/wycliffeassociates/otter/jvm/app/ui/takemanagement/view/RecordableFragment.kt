package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import com.jfoenix.controls.JFXSnackbar
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordableViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.progressdialog
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.*
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

abstract class RecordableFragment(protected val recordableViewModel: RecordableViewModel) : Fragment() {

    abstract fun createTakeCard(take: Take): TakeCard

    protected val audioPluginViewModel: AudioPluginViewModel by inject()

    /** Add custom components to this container, rather than root*/
    protected val mainContainer = VBox()

    protected val lastPlayOrPauseEvent: SimpleObjectProperty<PlayOrPauseEvent?> = SimpleObjectProperty()

    private val draggingNodeProperty = SimpleObjectProperty<Node>()

    // This inner class better organizes the components that need to be added to the derived class
    /** Use the provided functions to add these components to your derived class */
    inner class DragComponents {
        private val dragTargetBottom = VBox().apply {
            bindVisibleToDraggingNodeProperty()
        }

        fun dragTargetBottom(runOnNode: (VBox.() -> Unit)? = null): Node =
            dragTargetBottom.apply {
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
                    /* We can't just add the node being dragged, since the selected take might have just been
                        loaded from the database */
                    else -> add(createTakeCard(it))
                }
            }
        }
    }

    protected val dragComponents = DragComponents()
    private val dragTargetTop = dragComponents.dragTargetTop()

    init {
        importStylesheet<AppStyles>()
        createAudioPluginProgressDialog()
    }

    final override val root: Parent = anchorpane {
        addDragTakeEventHandlers()
        addButtonEventHandlers()

        createSnackBar(this)

        add(mainContainer.apply {
            anchorpaneConstraints {
                leftAnchor = 0.0
                rightAnchor = 0.0
                bottomAnchor = 0.0
                topAnchor = 0.0
            }
        })
    }

    private fun Parent.addDragTakeEventHandlers() {
        addEventHandler(StartDragEvent.START_DRAG, ::startDrag)
        addEventHandler(AnimateDragEvent.ANIMATE_DRAG, ::animateDrag)
        addEventHandler(CompleteDragEvent.COMPLETE_DRAG, ::completeDrag)
    }

    private fun Parent.addButtonEventHandlers() {
        addEventHandler(PlayOrPauseEvent.PLAY) {
            lastPlayOrPauseEvent.set(it)
        }
        addEventHandler(DeleteTakeEvent.DELETE_TAKE) {
            recordableViewModel.deleteTake(it.take)
        }
        addEventHandler(EditTakeEvent.EDIT_TAKE) {
            recordableViewModel.editTake(it)
        }
    }

    private fun createSnackBar(pane: Pane) {
        // TODO: This doesn't actually handle anything correctly. Need to know whether the user
        // TODO... hasn't selected an editor or recorder and respond appropriately.
        val snackBar = JFXSnackbar(pane)
        recordableViewModel.snackBarObservable.subscribe {
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

    private fun createAudioPluginProgressDialog() {
        // Plugin active cover
        val dialog = progressdialog {
            root.addClass(AppStyles.progressDialog)
            recordableViewModel.contextProperty.toObservable().subscribe { newContext ->
                graphic = when (newContext) {
                    TakeContext.RECORD -> AppStyles.recordIcon("60px")
                    TakeContext.EDIT_TAKES -> AppStyles.editIcon("60px")
                    null -> null
                }
            }
        }
        recordableViewModel.showPluginActiveProperty.onChange {
            Platform.runLater {
                if (it) dialog.open() else dialog.close()
            }
        }
    }

    private fun Node.bindVisibleToDraggingNodeProperty() {
        visibleProperty().bind(draggingNodeProperty.booleanBinding { it != null })
    }

    private fun getPointInRoot(node: Node, pointInNode: Point2D): Point2D {
        return when (node) {
            root -> pointInNode
            else -> getPointInRoot(node.parent, node.localToParent(pointInNode))
        }
    }

    private var dragStartDelta = Point2D(0.0, 0.0)

    private fun relocateDraggingNode(pointInRoot: Point2D) {
        draggingNodeProperty.value?.let { draggingNode ->
            val newX = pointInRoot.x - dragStartDelta.x
            val newY = pointInRoot.y - dragStartDelta.y
            draggingNode.relocate(newX, newY)
        }
    }

    private fun startDrag(evt: StartDragEvent) {
        if (evt.take != recordableViewModel.selectedTakeProperty.value) {
            val draggingNode = evt.draggingNode
            val mouseEvent = evt.mouseEvent
            dragStartDelta = Point2D(mouseEvent.x, mouseEvent.y)
            val pointInRoot = getPointInRoot(draggingNode, Point2D(mouseEvent.x, mouseEvent.y))

            root.add(draggingNode)
            draggingNode.toFront()

            draggingNodeProperty.set(draggingNode)
            relocateDraggingNode(pointInRoot)
        }
    }

    private fun animateDrag(evt: AnimateDragEvent) {
        draggingNodeProperty.value?.let { draggingNode ->
            val pointInRoot = getPointInRoot(draggingNode, Point2D(evt.mouseEvent.x, evt.mouseEvent.y))
            relocateDraggingNode(pointInRoot)
        }
    }

    private fun onDraggedToTarget(take: Take) {
        recordableViewModel.selectTake(take)
        draggingNodeProperty.value.removeFromParent()
    }

    private fun isDraggedToTarget(evt: MouseEvent): Boolean =
        dragTargetTop.contains(dragTargetTop.sceneToLocal(evt.sceneX, evt.sceneY))

    private fun completeDrag(evt: CompleteDragEvent) {
        if (isDraggedToTarget(evt.mouseEvent)) {
            onDraggedToTarget(evt.take)
        } else {
            evt.onCancel()
        }
        draggingNodeProperty.set(null)
    }
}