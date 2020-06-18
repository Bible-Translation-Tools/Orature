package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage

import com.jfoenix.transitions.CachedTransition
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import javafx.util.Duration
import org.wycliffeassociates.controls.ChromeableTabPane
import org.wycliffeassociates.otter.common.navigation.INavigator
import org.wycliffeassociates.otter.common.navigation.ITabGroup
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups.TabGroupBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.ui.mainscreen.view.MainScreenStyles
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

    enum class TransitionDirection {
        LEFT,
        RIGHT
    }

    init {
        root.apply {
            importStylesheet<MainScreenStyles>()
            addClass(Stylesheet.tabPane)

            // Disable builtin tab transition animation
            disableAnimationProperty().set(true)
            setTabSelectionAnimation()

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

    private fun setTabSelectionAnimation() {
        root.selectionModel.selectedIndexProperty().addListener { _, old, new ->
            val oldIndex = old.toInt()
            val newIndex = new.toInt()
            if (oldIndex >= 0 && newIndex >= 0) {
                val direction = if (oldIndex > newIndex) TransitionDirection.RIGHT else TransitionDirection.LEFT
                val tab: ChromeableTab? = root.tabs[newIndex] as? ChromeableTab
                if (tab != null) {
                    animateTabContent(tab.animatedContent, direction)
                }
            }
        }
    }

    // Animate first tab's content
    fun animateTabContent(direction: TransitionDirection) {
        if (root.tabs.size > 0) {
            if (root.selectionModel.selectedIndex > 0) {
                root.selectionModel.select(0)
            } else {
                val tab: ChromeableTab? = root.tabs[0] as? ChromeableTab
                if (tab != null) {
                    animateTabContent(tab.animatedContent, direction)
                }
            }
        }
    }

    private fun animateTabContent(content: Node, direction: TransitionDirection) {
        val contentWidth = when (direction) {
            TransitionDirection.LEFT -> root.width
            TransitionDirection.RIGHT -> -root.width
        }

        content.translateX = contentWidth

        object : CachedTransition(
            content,
            Timeline(
                KeyFrame(
                    Duration.millis(1000.0),
                    KeyValue(
                        content.translateXProperty(),
                        0.0,
                        Interpolator.EASE_BOTH
                    )
                )
            )
        ) {
            init {
                cycleDuration = Duration.seconds(0.320)
                delay = Duration.seconds(0.0)
            }
        }.play()
    }
}
