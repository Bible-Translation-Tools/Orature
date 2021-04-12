package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Control
import javafx.scene.layout.VBox
import javafx.util.Duration
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.card.events.DeleteTakeEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.PlayOrPauseEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.TakeEvent
import org.wycliffeassociates.otter.jvm.controls.dialog.PluginOpenedPage
import org.wycliffeassociates.otter.jvm.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.controls.dragtarget.events.AnimateDragEvent
import org.wycliffeassociates.otter.jvm.controls.dragtarget.events.CompleteDragEvent
import org.wycliffeassociates.otter.jvm.controls.dragtarget.events.StartDragEvent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordableViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

abstract class RecordableFragment(
    protected val recordableViewModel: RecordableViewModel,
    dragTargetBuilder: DragTargetBuilder
) : Fragment() {

    override fun onUndock() {
        super.onUndock()
        closePlayers()
    }

    override fun onDock() {
        super.onDock()
        openPlayers()
    }

    private val logger = LoggerFactory.getLogger(RecordableFragment::class.java)

    abstract fun createTakeCard(take: TakeCardModel): Control

    private val pluginOpenedPage: PluginOpenedPage
    protected val audioPluginViewModel: AudioPluginViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    private val navigator: NavigationMediator by inject()

    /** Add custom components to this container, rather than root*/
    protected val mainContainer = VBox()

    protected val lastPlayOrPauseEvent: SimpleObjectProperty<PlayOrPauseEvent> = SimpleObjectProperty()

    private val draggingNodeProperty = SimpleObjectProperty<Node>()

    val dragTarget =
        dragTargetBuilder
            .build(draggingNodeProperty.booleanBinding { it != null })
            .apply {
                recordableViewModel.selectedTakeProperty.onChangeAndDoNow { take ->
                    /* We can't just add the node being dragged, since the selected take might have just been
                        loaded from the database */
                    this.selectedNodeProperty.value = take?.let { createTakeCard(take) }
                }
            }

    private val dragContainer = VBox().apply {
        this.prefWidthProperty().bind(dragTarget.widthProperty())
        draggingNodeProperty.onChange { draggingNode ->
            (dragTarget.selectedNodeProperty.get() as? TakeCard)?.simpleAudioPlayer?.close()
            clear()
            draggingNode?.let { add(draggingNode) }
        }
    }

    init {
        importStylesheet<AppStyles>()
        pluginOpenedPage = createPluginOpenedPage()
        navigator.subscribe<PluginOpenedEvent> { pluginInfo ->
            if (!pluginInfo.isNative) {
                navigator.dock(pluginOpenedPage)
            }
        }
        navigator.subscribe<PluginClosedEvent> {
            (navigator.dockedComponent as? PluginOpenedPage)?.let {
                navigator.back()
            }
            openPlayers()
        }
    }

    final override val root: Parent = anchorpane {
        addDragTakeEventHandlers()
        addButtonEventHandlers()

        createSnackBar()

        add(mainContainer
            .apply {
                anchorpaneConstraints {
                    leftAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                    topAnchor = 0.0
                }
            }
        )
        add(dragContainer)
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
        addEventHandler(TakeEvent.EDIT_TAKE) {
            closePlayers()
            recordableViewModel.processTakeWithPlugin(it, PluginType.EDITOR)
        }
        addEventHandler(TakeEvent.MARK_TAKE) {
            closePlayers()
            recordableViewModel.processTakeWithPlugin(it, PluginType.MARKER)
        }
    }

    abstract fun closePlayers()

    abstract fun openPlayers()

    private fun createSnackBar() {
        // TODO: This doesn't actually handle anything correctly. Need to know whether the user
        // TODO... hasn't selected an editor or recorder and respond appropriately.
        recordableViewModel
            .snackBarObservable
            .doOnError { e ->
                logger.error("Error in creating no plugin snackbar", e)
            }
            .subscribe { pluginErrorMessage ->
                SnackbarHandler.enqueue(
                    JFXSnackbar.SnackbarEvent(
                        JFXSnackbarLayout(
                            pluginErrorMessage,
                            messages["addPlugin"].toUpperCase(),
                            EventHandler {
                                audioPluginViewModel.addPlugin(true, false)
                            }
                        ),
                        Duration.millis(5000.0),
                        null
                    )
                )
            }
    }

    private fun createPluginOpenedPage(): PluginOpenedPage {
        // Plugin active cover
        return PluginOpenedPage().apply {
            dialogTitleProperty.bind(recordableViewModel.dialogTitleBinding())
            dialogTextProperty.bind(recordableViewModel.dialogTextBinding())
            playerProperty.bind(recordableViewModel.sourceAudioPlayerProperty)
            audioAvailableProperty.bind(recordableViewModel.sourceAudioAvailableProperty)
            sourceTextProperty.bind(workbookDataStore.sourceTextBinding())
            sourceContentTitleProperty.bind(workbookDataStore.activeChunkTitleBinding())
        }
    }

    private fun getPointInRoot(node: Node, pointInNode: Point2D): Point2D {
        return when (node) {
            root -> pointInNode
            else -> getPointInRoot(node.parent, node.localToParent(pointInNode))
        }
    }

    private fun getBoundsInRoot(node: Node, bounds: Bounds): Bounds {
        return when (node) {
            root -> bounds
            else -> getBoundsInRoot(node.parent, node.localToParent(bounds))
        }
    }

    private var dragStartDelta = Point2D(0.0, 0.0)

    private fun relocateDragContainer(pointInRoot: Point2D) {
        val newX = pointInRoot.x - dragStartDelta.x
        val newY = pointInRoot.y - dragStartDelta.y
        dragContainer.relocate(newX, newY)
    }

    private fun startDrag(event: StartDragEvent) {
        if (event.take != recordableViewModel.selectedTakeProperty.value?.take) {
            val draggingNode = event.draggingNode
            val mouseEvent = event.mouseEvent
            dragStartDelta = Point2D(mouseEvent.x, mouseEvent.y)
            val pointInRoot = getPointInRoot(draggingNode, Point2D(mouseEvent.x, mouseEvent.y))

            draggingNodeProperty.set(draggingNode)
            dragContainer.toFront()
            relocateDragContainer(pointInRoot)
        }
    }

    private fun animateDrag(event: AnimateDragEvent) {
        draggingNodeProperty.value?.let { draggingNode ->
            val pointInRoot = getPointInRoot(draggingNode, Point2D(event.mouseEvent.x, event.mouseEvent.y))
            relocateDragContainer(pointInRoot)
        }
    }

    private fun isDraggedToTarget(): Boolean {
        val draggingNodeBounds =
            getBoundsInRoot(draggingNodeProperty.value.parent, draggingNodeProperty.value.boundsInParent)
        val dragTargetBounds = getBoundsInRoot(dragTarget.parent, dragTarget.boundsInParent)

        // Reduce the bounds of the drag target slightly so that we are sure there is enough overlap
        // to trigger select take
        val dragTargetReducedBounds = dragTargetBounds.let {
            BoundingBox(
                it.minX + it.width * .05,
                it.minY + it.height * .05,
                it.width * .9,
                it.height * .9
            )
        }
        return draggingNodeBounds.intersects(dragTargetReducedBounds)
    }

    private fun completeDrag(event: CompleteDragEvent) {
        if (draggingNodeProperty.value != null) {
            if (isDraggedToTarget()) {
                recordableViewModel.selectTake(event.take)
            } else {
                event.onCancel()
            }
            draggingNodeProperty.set(null)
        }
    }
}
