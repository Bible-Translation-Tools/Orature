package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage

import com.jfoenix.transitions.CachedTransition
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.Node
import javafx.util.Duration
import org.wycliffeassociates.controls.ChromeableTabPane

enum class TransitionDirection {
    LEFT,
    RIGHT
}

class AnimatedChromeableTabPane(chrome: Node, headerScalingFactor: Double) :
    ChromeableTabPane(chrome, headerScalingFactor) {

    init {
        // Disable builtin JFoenix tab transition animation
        disableAnimationProperty().set(true)

        setTabSelectionAnimation()
    }

    private fun setTabSelectionAnimation() {
        selectionModel.selectedIndexProperty().addListener { _, old, new ->
            val oldIndex = old.toInt()
            val newIndex = new.toInt()
            if (oldIndex >= 0 && newIndex >= 0) {
                val direction = if (oldIndex > newIndex) TransitionDirection.RIGHT else TransitionDirection.LEFT
                val tab: AnimatedTab? = tabs[newIndex] as? AnimatedTab
                if (tab != null) {
                    animate(tab.animatedContent, direction)
                }
            }
        }
    }

    // Animate first tab's content
    fun animate(direction: TransitionDirection) {
        if (tabs.size > 0) {
            if (selectionModel.selectedIndex > 0) {
                selectionModel.select(0)
            } else {
                val tab: AnimatedTab? = tabs[0] as? AnimatedTab
                if (tab != null) {
                    animate(tab.animatedContent, direction)
                }
            }
        }
    }

    private fun animate(content: Node, direction: TransitionDirection) {
        val contentWidth = when (direction) {
            TransitionDirection.LEFT -> width
            TransitionDirection.RIGHT -> -width
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