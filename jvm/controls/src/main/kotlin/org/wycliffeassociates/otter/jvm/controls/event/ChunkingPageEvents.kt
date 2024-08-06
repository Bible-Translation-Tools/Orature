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
package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import tornadofx.FXEvent
import java.io.File

class ChunkingStepSelectedEvent(val step: ChunkingStep) : FXEvent()
class ChunkSelectedEvent(val chunkNumber: Int): FXEvent()
class ChunkExportedEvent(val chunkTake: Take, val outputFile: File): FXEvent()
class ChunkTakeEvent(val take: Take, val action: TakeAction): FXEvent()
enum class TakeAction {
    SELECT,
    EDIT,
    DELETE
}
class MarkerDeletedEvent(val markerId: Int): FXEvent()

/**
 * @param markerId the id (not index) of the marker
 * @param start starting frame position of the movement
 * @param end ending frame position of the movement
 */
class MarkerMovedEvent(val markerId: Int, val start: Int, val end: Int): FXEvent()
class UndoChunkingPageEvent: FXEvent()
class RedoChunkingPageEvent: FXEvent()
class GoToNextChapterEvent: FXEvent()
class GoToPreviousChapterEvent: FXEvent()
class OpenInPluginEvent: FXEvent()

/**
 * Use this event to avoid unwanted auto-navigation or state refresh
 * when returning from an external plugin. Only fire this event inside
 * onDock() of main view in Translation.
 */
class ReturnFromPluginEvent: FXEvent()