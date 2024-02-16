package org.wycliffeassociates.otter.common.domain.translation

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