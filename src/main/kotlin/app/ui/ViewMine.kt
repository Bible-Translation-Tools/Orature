package app.ui;
import javafx.geometry.Insets
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import tornadofx.*;
import java.time.Duration

class ViewMine: View() {

    override val root = BorderPane()

    init {
        with(root) {

            left {
                pane() {

                    val blueCircle = circle{
                        centerX = 100.0
                        centerY = 100.0
                        radius = 100.0;
                        fill = Color.DARKSLATEBLUE;
                    }

                    val greenCircle = circle{
                        centerX = 100.0
                        centerY = 100.0
                        radius = 50.0;
                        fill = Color.ANTIQUEWHITE;
                    }

                    val arc = arc {
                        fill = Color.DARKSLATEBLUE;
                        centerX = 100.0
                        centerY = 100.0
                        radiusX = 51.0
                        radiusY = 51.0
                        startAngle = 0.0
                        type = ArcType.ROUND
                    }

                    add(blueCircle);
                    add(greenCircle);
                    add(arc);
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
