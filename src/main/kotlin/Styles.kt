import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.shape.StrokeType
import tornadofx.*;

class Styles : Stylesheet() {
    companion object {
        // Define our styles
        val wrapper by cssclass()
        val bob by cssclass()
        val alice by cssclass()

        // Define our colors
        val dangerColor = c("#a94442")
        val hoverColor = c("#d49942")
    }

    init {
        wrapper {
            padding = box(10.px)
            spacing = 10.px
        }

        label {
            fontSize = 56.px
            padding = box(5.px, 10.px)
            maxWidth = infinity

            and(bob, alice) {
                borderColor += box(dangerColor)
                borderStyle += BorderStrokeStyle(StrokeType.INSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10.0, 0.0, listOf(25.0, 5.0))
                borderWidth += box(5.px)

                and(hover) {
                    backgroundColor += hoverColor
                }
            }
        }
    }
}