package app.ui.widgets;
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import tornadofx.*;

class RecordingAnimation: HBox() {


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
        timeline {
            keyframe(javafx.util.Duration.millis(3000.0)) {
                keyvalue(arc.lengthProperty(),-360.0)
            }
        }
    }

    init {
        with(root) {

        }
    }

}
