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

import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import tornadofx.FXEvent


class BeginRecordingEvent(val index: Int, val chunk: Chunk) : FXEvent()
class NextVerseEvent(val index: Int, val chunk: Chunk) : FXEvent()
class PauseRecordingEvent(val index: Int, val chunk: Chunk) : FXEvent()
class PauseRecordAgainEvent(val index: Int, val chunk: Chunk) : FXEvent()
class ResumeRecordingEvent(val index: Int, val chunk: Chunk) : FXEvent()
class ResumeRecordingAgainEvent(val index: Int, val chunk: Chunk) : FXEvent()
class RecordVerseEvent(val index: Int, val chunk: Chunk) : FXEvent()
class RecordAgainEvent(val index: Int) : FXEvent()
class SaveRecordingEvent(val index: Int) : FXEvent()
class PlayVerseEvent(val verse: AudioMarker) : FXEvent()
class PlayChapterEvent() : FXEvent()
class PauseVerseEvent(val verse: AudioMarker) : FXEvent()
class OpenInAudioPluginEvent(val index: Int) : FXEvent()
class ChapterReturnFromPluginEvent : FXEvent()