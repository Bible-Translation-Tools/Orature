package org.wycliffeassociates.otter.jvm.app.widgets.recordButton;

import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcType
import javafx.scene.shape.Circle
import tornadofx.*
import java.time.Duration

class RecordingAnimation : HBox() {
    var animation: Timeline? = null
    var arc = arc {
        centerX = 120.0
        centerY = 120.0
        radiusX = 120.0
        radiusY = 120.0
        startAngle = -270.0
        type = ArcType.ROUND
    }
    var circle = circle {
        centerX = 120.0
        centerY = 120.0
        radius = 120.0
        fill = c("#EDEDED")
    }


    init {
        alignment = Pos.CENTER
        pane {
            add(circle)
            add(arc)
        }
    }

    fun animate(arcFill: String) {
        arc.fill = (c(arcFill))
        animation = timeline {
            keyframe(javafx.util.Duration.millis(3000.0)) {
                keyvalue(arc.lengthProperty(), -360.0)
            }
        }
    }

    fun stop() {
        animation?.pause()
    }

    fun reset() {
        animation = timeline {
            keyframe(javafx.util.Duration.millis(100.0)) {
                keyvalue(arc.lengthProperty(), 0.0)
            }
        }
    }
}