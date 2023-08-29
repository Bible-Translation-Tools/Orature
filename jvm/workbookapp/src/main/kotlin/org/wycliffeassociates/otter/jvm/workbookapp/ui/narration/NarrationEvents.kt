package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import tornadofx.FXEvent

class RecordAgainEvent(val index: Int) : FXEvent()
class PlayVerseEvent(val verse: VerseMarker) : FXEvent()
class PlayChapterEvent() : FXEvent()
class PauseEvent(): FXEvent()
class OpenInAudioPluginEvent(val index: Int) : FXEvent()
class ChapterReturnFromPluginEvent : FXEvent()
class OpenChapterEvent(val chapter: Chapter) : FXEvent()

