package org.wycliffeassociates.otter.common.domain

interface IUndoable {
    fun execute()

    fun undo()

    fun redo()
}
