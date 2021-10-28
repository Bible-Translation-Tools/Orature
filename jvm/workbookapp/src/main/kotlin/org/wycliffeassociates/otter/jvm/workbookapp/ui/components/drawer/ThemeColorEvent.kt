package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import org.wycliffeassociates.otter.common.data.ColorTheme
import tornadofx.FXEvent
import tornadofx.UIComponent
import kotlin.reflect.KClass

class ThemeColorEvent<T: UIComponent>(val type: KClass<T>, val data: ColorTheme): FXEvent()