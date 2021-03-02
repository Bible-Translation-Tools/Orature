package org.wycliffeassociates.otter.jvm.workbookapp.plugin

import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import tornadofx.FXEvent

class PluginOpenedEvent(val type: PluginType) : FXEvent()

class PluginClosedEvent(val type: PluginType) : FXEvent()
