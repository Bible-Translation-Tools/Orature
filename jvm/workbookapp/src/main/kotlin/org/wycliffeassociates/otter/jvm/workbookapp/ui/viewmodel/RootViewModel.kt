package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*

class RootViewModel : ViewModel() {
    val pluginOpenedProperty = SimpleBooleanProperty(false)
}
