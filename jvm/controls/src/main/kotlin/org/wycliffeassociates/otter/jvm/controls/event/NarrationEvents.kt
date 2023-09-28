package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import tornadofx.FXEvent


class BeginRecordingEvent(val index: Int, val chunk: Chunk): FXEvent()
class NextVerseEvent(val index: Int, val chunk: Chunk) : FXEvent()
class PauseRecordingEvent(val index: Int, val chunk: Chunk): FXEvent()
class ResumeRecordingEvent(val index: Int, val chunk: Chunk) : FXEvent()
class RecordVerseEvent(val index: Int, val chunk: Chunk) : FXEvent()
class RecordAgainEvent(val index: Int) : FXEvent()
class SaveRecordingEvent(val index: Int) : FXEvent()
class PlayVerseEvent(val verse: VerseMarker) : FXEvent()
class PlayChapterEvent() : FXEvent()
class PauseEvent(): FXEvent()
class OpenInAudioPluginEvent(val index: Int) : FXEvent()
class ChapterReturnFromPluginEvent : FXEvent()
class OpenChapterEvent(val chapter: Chapter) : FXEvent()
