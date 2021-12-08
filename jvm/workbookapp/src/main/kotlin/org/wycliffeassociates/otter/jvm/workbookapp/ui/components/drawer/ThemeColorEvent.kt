package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import tornadofx.FXEvent
import tornadofx.UIComponent
import kotlin.reflect.KClass

class ThemeColorEvent<T : UIComponent>(val type: KClass<T>, val action: ChangeThemeEventAction) : FXEvent()
enum class ChangeThemeEventAction {
    LIGHT,
    DARK
}