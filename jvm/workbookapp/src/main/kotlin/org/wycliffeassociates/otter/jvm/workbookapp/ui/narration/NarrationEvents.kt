package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import tornadofx.FXEvent

class RecordAgainEvent(val index: Int) : FXEvent()
class PlayVerseEvent(val verse: VerseNode) : FXEvent()
class OpenInAudioPluginEvent(val index: Int) : FXEvent()
class ChapterReturnFromPluginEvent: FXEvent()
