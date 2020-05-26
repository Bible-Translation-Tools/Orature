package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.TabPane
import org.wycliffeassociates.otter.common.navigation.ITabGroup
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import tornadofx.Component

abstract class TabGroup : Component(), ITabGroup {
    private val chromeableStage: ChromeableStage by inject()
    protected val tabPane: TabPane = chromeableStage.tabPane

    val showHorizontalNavBarProperty = SimpleBooleanProperty(false)

    init {
        chromeableStage.showHorizontalNavBarProperty.bind(showHorizontalNavBarProperty)
    }

    override fun deactivate() {
        showHorizontalNavBarProperty.set(false)
    }
}