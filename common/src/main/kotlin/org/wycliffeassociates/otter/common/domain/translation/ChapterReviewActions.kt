package org.wycliffeassociates.otter.common.domain.translation

import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.domain.IUndoable
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementModel

class AddMarkerAction(
    private val markerModel: MarkerPlacementModel,
    private val location: Int
) : IUndoable {
    private var markerId: Int? = null

    override fun execute() {
        markerId = markerModel.addMarker(location)
    }

    override fun undo() {
        markerId?.let {
            markerModel.deleteMarker(it)
        }
    }

    override fun redo() = execute()
}

class DeleteMarkerAction(
    private val markerModel: MarkerPlacementModel,
    id: Int
) : IUndoable {
    private var markerId: Int = id
    private var location: Int? = null

    override fun execute() {
        location = markerModel.markerItems.find { it.id == markerId }?.marker?.location
        markerModel.deleteMarker(markerId)
    }

    override fun undo() {
        location?.let { loc ->
            markerId = markerModel.addMarker(loc)
        }
    }

    override fun redo() = execute()
}

class MoveMarkerAction(
    private val markerModel: MarkerPlacementModel,
    private val id: Int,
    private val from: Int,
    private val to: Int
) : IUndoable {
    override fun execute() {
        markerModel.moveMarker(id, from, to)
    }

    override fun undo() {
        markerModel.moveMarker(id, to, from)
    }

    override fun redo() = execute()
}

class TakeEditAction(
    private val audio: AssociatedAudio,
    private val newTake: Take,
    private val oldTake: Take,
) : IUndoable {

    lateinit var newMarkerModel: MarkerPlacementModel

    private var undoCallback: () -> Unit = {}
    private var redoCallback: () -> Unit = {}

    override fun execute() {
        newTake.checkingState.accept(
            TakeCheckingState(
                CheckingStatus.VERSE,
                newTake.checksum()
            )
        )
        oldTake.deletedTimestamp.accept(DateHolder.now())
    }

    override fun undo() {
        oldTake.deletedTimestamp.accept(DateHolder.empty)
        audio.selectTake(oldTake)
        newTake.deletedTimestamp.accept(DateHolder.now())
        undoCallback()
    }

    override fun redo() {
        audio.selectTake(newTake)
        oldTake.deletedTimestamp.accept(DateHolder.now())
        redoCallback()
    }

    fun setUndoCallback(op: () -> Unit) { undoCallback = op }
    fun setRedoCallback(op: () -> Unit) { redoCallback = op }
}