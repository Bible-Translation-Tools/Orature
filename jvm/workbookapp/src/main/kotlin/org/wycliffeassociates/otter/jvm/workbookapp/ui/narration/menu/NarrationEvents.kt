package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu

import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import tornadofx.FXEvent

class NarrationRedoEvent : FXEvent()
class NarrationUndoEvent : FXEvent()
class NarrationOpenInPluginEvent(val plugin: PluginType) : FXEvent()
class NarrationRestartChapterEvent: FXEvent()