package app.ui

import javafx.scene.Node
import afester.javafx.svg.SvgLoader
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.io.File

//Loads an image with a given file path
fun imageLoader(imagePathToLoad: File): Node {
    val ext: String = imagePathToLoad.extension

    when (ext) {
        //if file extension is ".svg", return a Group node
        "svg" -> return SvgLoader().loadSvg(imagePathToLoad.toString())

        //if file extension is ".png" or ".jpg", return an ImageView node
        "png","jpg" -> return ImageView(Image(imagePathToLoad.inputStream()))

        else -> {
            println("Error: Image file extension found is not svg, png, or jpg")
            return ImageView(/*put a default image here*/)
        }
    }
}



