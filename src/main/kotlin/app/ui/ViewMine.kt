package app.ui;
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import tornadofx.*;

class ViewMine: View() {

    override val root = BorderPane()

    init {
        with(root) {

            left {
                pane() {

                    val bigCircle = circle{
                        centerX = 100.0
                        centerY = 100.0
                        radius = 100.0;
                        fill = Color.DIMGREY;
                    }

                    val mediumCircle = circle{
                        centerX = 100.0
                        centerY = 100.0
                        radius = 90.0;
                        fill = Color.ANTIQUEWHITE;
                    }

                    val arc = arc {
                        fill = Color.TOMATO;
                        centerX = 100.0
                        centerY = 100.0
                        radiusX = 91.0
                        radiusY = 91.0
                        startAngle = 0.0
                        type = ArcType.ROUND
                    }

                    val smallCircle = circle{
                        centerX = 100.0
                        centerY = 100.0
                        radius = 20.0;
                        fill = Color.DIMGREY;
                    }

                    timeline {
                        keyframe(javafx.util.Duration.millis(3000.0)) {
                            keyvalue(arc.lengthProperty(),360.0)
                        }
                    }
                }

            }
        }
    }
}
