package org.wycliffeassociates.otter.jvm.app.ui.chromeablestage

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import org.wycliffeassociates.controls.ChromeableTabPane
import org.wycliffeassociates.otter.common.navigation.INavigator
import org.wycliffeassociates.otter.common.navigation.ITabGroup
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.app.ui.chromeablestage.tabgroups.TabGroupBuilder
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view.MainScreenStyles
import tornadofx.*
import java.util.*

class ChromeableStage : UIComponent(), ScopedInstance, INavigator {
    val chrome: Node by param()
    val headerScalingFactor: Double by param()
    val canNavigateBackProperty = SimpleBooleanProperty(false)

    override val tabGroupMap: MutableMap<TabGroupType, ITabGroup> = mutableMapOf()
    override val navBackStack = Stack<ITabGroup>()
    override val tabGroupBuilder = TabGroupBuilder()
    override val root = ChromeableTabPane(chrome, headerScalingFactor)

    override var currentGroup: ITabGroup? = null

    init {
        root.apply {
            importStylesheet<MainScreenStyles>()
            addClass(Stylesheet.tabPane)

            // Using a size property binding and toggleClass() did not work consistently. This does.
            tabs.onChange {
                if (it.list.size == 1) {
                    addClass(MainScreenStyles.singleTab)
                } else {
                    removeClass(MainScreenStyles.singleTab)
                }
            }
        }
    }

    override fun back() {
        clearTabs()
        super.back()
        setCanNavigateBack()
    }

    override fun navigateTo(tabGroupType: TabGroupType) {
        clearTabs()
        super.navigateTo(tabGroupType)
        setCanNavigateBack()
    }

    private fun setCanNavigateBack() {
        canNavigateBackProperty.set(navBackStack.isNotEmpty())
    }

    private fun clearTabs() {
        root.tabs.clear()
    }
}
