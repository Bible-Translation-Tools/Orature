package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import tornadofx.*
import kotlin.reflect.KClass

class DrawerEvent<T: UIComponent>(val type: KClass<T>, val action: DrawerEventAction): FXEvent()
enum class DrawerEventAction {
    OPEN,
    CLOSE
}
