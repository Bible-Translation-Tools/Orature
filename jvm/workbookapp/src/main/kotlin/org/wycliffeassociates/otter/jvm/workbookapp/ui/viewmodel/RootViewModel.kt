package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*

class RootViewModel : ViewModel() {
    private val settingsViewModel: SettingsViewModel by inject()
    val pluginOpenedProperty = SimpleBooleanProperty(false)

    init {
        settingsViewModel.refreshPlugins()
    }
}
