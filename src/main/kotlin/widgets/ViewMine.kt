package widgets;
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import tornadofx.*;

class ViewMine: HBox() {

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

//                 val bigCircle = circle{
//                     centerX = 100.0
//                     centerY = 100.0
//                     radius = 100.0;
//                     fill = Color.DIMGREY;
//                 }

//                 val mediumCircle = circle{
//                     centerX = 100.0
//                     centerY = 100.0
//                     radius = 90.0;
//                     fill = Color.ANTIQUEWHITE;
//
//                 }

                 add(arc)

//                 val smallCircle = circle{
//                     centerX = 100.0
//                     centerY = 100.0
//                     radius = 20.0;
//                     fill = Color.DIMGREY;
//                 }


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
