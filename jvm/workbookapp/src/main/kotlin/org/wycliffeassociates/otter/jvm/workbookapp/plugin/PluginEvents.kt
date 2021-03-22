package org.wycliffeassociates.otter.jvm.workbookapp.plugin

import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import tornadofx.FXEvent

class PluginOpenedEvent(val type: PluginType, val isNative: Boolean) : FXEvent()

class PluginClosedEvent(val type: PluginType) : FXEvent()
