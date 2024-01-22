/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.model

import org.wycliffeassociates.otter.common.domain.IUndoable
import java.util.*
import java.util.ArrayDeque

class UndoableActionHistory<T : IUndoable> {
    private val undoStack: Deque<IUndoable> = ArrayDeque()
    private val redoStack: Deque<IUndoable> = ArrayDeque()

    fun execute(action: T) {
        undoStack.push(action)
        redoStack.clear()
        action.execute()
    }

    fun undo() {
        if (undoStack.isEmpty()) return

        val op = undoStack.pop()
        redoStack.push(op)
        op.undo()
    }

    fun redo() {
        if (redoStack.isEmpty()) return

        val action = redoStack.pop()
        undoStack.push(action)
        action.redo()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()
}