package org.wycliffeassociates.otter.jvm.app.ui

import javafx.scene.Node
import afester.javafx.svg.SvgLoader
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import tornadofx.add
import tornadofx.doubleBinding
import java.io.File


//Loads an image with a given file path
//How to use is described below
fun imageLoader(imagePathToLoad: File): Node {
    val ext: String = imagePathToLoad.extension
    when (ext) {

        //if file extension is ".svg", return a Group node
        "svg" -> {
            return StackPane().apply {
                val svgImage = SvgLoader().loadSvg(imagePathToLoad.toString())
                add(svgImage)
                // Setup bindings so svg scales to fit Node
                svgImage.scaleXProperty().bind(
                        widthProperty().doubleBinding {
                            (it?.toDouble() ?: 0.0) / svgImage.boundsInLocal.width
                        }
                )
                svgImage.scaleYProperty().bind(
                        heightProperty().doubleBinding {
                            (it?.toDouble() ?: 0.0) / svgImage.boundsInLocal.height
                        }
                )
                minHeight = 0.0
                minWidth = 0.0
            }
        }

        //if file extension is ".png" or ".jpg", return an ImageView node
        "png", "jpg" -> return ImageView(Image(imagePathToLoad.inputStream()))

        else -> {
            println("Error: Image file extension found is not svg, png, or jpg")
            return ImageView(/*put a default image here*/)
        }
    }
}
