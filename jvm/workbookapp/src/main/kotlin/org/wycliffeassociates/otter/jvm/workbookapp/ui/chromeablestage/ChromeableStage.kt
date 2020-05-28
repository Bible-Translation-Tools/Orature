package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.controls.ChromeableTabPane
import org.wycliffeassociates.otter.common.navigation.INavigator
import org.wycliffeassociates.otter.common.navigation.ITabGroup
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.controls.resourcenavbar.ResourceNavBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups.TabGroupBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.ui.mainscreen.view.MainScreenStyles
import tornadofx.*
import java.util.*

class ChromeableStage : UIComponent(), ScopedInstance, INavigator {
    val chrome: Node by param()
    val headerScalingFactor: Double by param()
    val canNavigateBackProperty = SimpleBooleanProperty(false)
    val resourceNavBarVisibleProperty = SimpleBooleanProperty(false)
    val tabPane = ChromeableTabPane(chrome, headerScalingFactor)
    val resourceNavBar = ResourceNavBar()

    override val tabGroupMap: MutableMap<TabGroupType, ITabGroup> = mutableMapOf()
    override val navBackStack = Stack<ITabGroup>()
    override val tabGroupBuilder = TabGroupBuilder()
    override val root = VBox()

    override var currentGroup: ITabGroup? = null

    init {
        root.apply {
            importStylesheet<MainScreenStyles>()

            tabPane.apply {
                addClass(Stylesheet.tabPane)
                vgrow = Priority.ALWAYS

                // Using a size property binding and toggleClass() did not work consistently. This does.
                tabs.onChange {
                    if (it.list.size == 1) {
                        addClass(MainScreenStyles.singleTab)
                    } else {
                        removeClass(MainScreenStyles.singleTab)
                    }
                }
            }
            add(tabPane)

            resourceNavBar.apply {
                visibleWhen { resourceNavBarVisibleProperty }
                managedWhen { visibleProperty() }
                prefHeight = 70.0

                previousButtonTextProperty().set(messages["previousChunk"])
                nextButtonTextProperty().set(messages["nextChunk"])

                importStylesheet(userAgentStylesheet)
            }
            add(resourceNavBar)
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
        tabPane.tabs.clear()
    }
}
