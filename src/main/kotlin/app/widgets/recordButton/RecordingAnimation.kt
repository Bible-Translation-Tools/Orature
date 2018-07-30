package app.widgets.recordButton;

import app.UIColorsObject.Colors
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import tornadofx.*
import java.time.Duration

class RecordingAnimation : HBox() {
    var animation: Timeline? = null
    val bigCircle = circle {
        centerX = 120.0
        centerY = 120.0
        radius = 120.0;
        fill = c(Colors["baseBackground"])
    }
    var arc = arc {
        centerX = 120.0
        centerY = 120.0
        radiusX = 120.0
        radiusY = 120.0
        startAngle = -270.0
        type = ArcType.ROUND

        style {
            backgroundColor += Color.TRANSPARENT
        }
    }
    val root = pane {
        alignment = Pos.CENTER
        add(bigCircle)
        add(arc)
    }
    fun animate() {
        arc.fill = (c(Colors["primary"]))
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
               // arc.fill = c("#EDEDED")
                keyvalue(arc.lengthProperty(), 0.0)
            }
        }
    }
}


