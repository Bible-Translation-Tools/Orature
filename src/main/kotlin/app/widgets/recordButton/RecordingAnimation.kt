package app.widgets.recordButton;
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import tornadofx.*;

class RecordingAnimation: HBox() {

    lateinit var animation: Timeline

    val bigCircle = circle{
             centerX = 120.0
             centerY = 120.0
             radius = 120.0;
             fill = c("#EDEDED");
         }

        val  arc = arc {
        fill = c("#CC4141");
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

     val root = pane{

         alignment = Pos.CENTER

                 add(bigCircle)
                 add(arc)
    }

    fun animate() {
        animation = timeline {
            keyframe(javafx.util.Duration.millis(3000.0)) {
                keyvalue(arc.lengthProperty(),-360.0)
            }
        }
    }


    fun stop() {
        animation.pause()
    }

    fun resetAnimation() {
        animation = timeline {
            keyframe(javafx.util.Duration.millis(0.0)) {
                keyvalue(arc.lengthProperty(),-270.0)
            }
        }
    }



    init {
        with(root) {

        }
    }




}
