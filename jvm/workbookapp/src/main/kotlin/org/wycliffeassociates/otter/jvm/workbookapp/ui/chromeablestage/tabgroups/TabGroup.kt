package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import javafx.scene.control.TabPane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.navigation.ITabGroup
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import tornadofx.Component

abstract class TabGroup : Component(), ITabGroup {
    private val chromeableStage: ChromeableStage by inject()
    protected val root: VBox = chromeableStage.root
    protected val tabPane: TabPane = chromeableStage.tabPane

    override fun deactivate() {
        // Default is no-op
    }
}