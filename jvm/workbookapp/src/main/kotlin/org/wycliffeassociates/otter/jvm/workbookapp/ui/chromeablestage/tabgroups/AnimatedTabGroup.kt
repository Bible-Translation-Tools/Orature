package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import com.jfoenix.controls.JFXTabPane
import com.jfoenix.transitions.CachedTransition
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.Node
import javafx.util.Duration

abstract class AnimatedTabGroup : TabGroup() {

    init {
        setTabSelectionAnimation()
    }

    private fun setTabSelectionAnimation() {
        tabPane.selectionModel.selectedIndexProperty().addListener { _, old, new ->
            val oldIndex = old.toInt()
            val newIndex = new.toInt()
            if (oldIndex >= 0 && newIndex >= 0) {
                val direction =
                    if (oldIndex > newIndex) TransitionDirection.RIGHT else TransitionDirection.LEFT
                val tab: AnimatedTab? = tabPane.tabs[newIndex] as? AnimatedTab
                if (tab != null) {
                    animate(tab.animatedContent, direction)
                }
            }
        }
    }

    // Animate first tab's content
    fun animate(direction: TransitionDirection) {
        if (tabPane.tabs.size > 0) {
            if (tabPane.selectionModel.selectedIndex > 0) {
                tabPane.selectionModel.select(0)
            } else {
                val tab: AnimatedTab? = tabPane.tabs[0] as? AnimatedTab
                if (tab != null) {
                    animate(tab.animatedContent, direction)
                }
            }
        }
    }

    private fun animate(content: Node, direction: TransitionDirection) {
        val contentWidth = when (direction) {
            TransitionDirection.LEFT -> tabPane.width
            TransitionDirection.RIGHT -> -tabPane.width
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

    override fun activate() {
        (tabPane as? JFXTabPane)?.disableAnimationProperty()?.set(true)
    }

    override fun deactivate() {
        (tabPane as? JFXTabPane)?.disableAnimationProperty()?.set(false)
    }
}