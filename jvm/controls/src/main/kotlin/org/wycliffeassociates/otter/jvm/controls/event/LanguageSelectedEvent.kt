package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.primitives.Language
import tornadofx.FXEvent

class LanguageSelectedEvent(val item: Language) : FXEvent()
