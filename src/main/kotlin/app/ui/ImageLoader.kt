package app.ui
import javafx.scene.Node
import afester.javafx.svg.SvgLoader
import javafx.scene.image.Image
import javafx.scene.image.ImageView

import java.io.File

fun imageLoader(imagePathToLoad: File): Node {
    val ext: String = imagePathToLoad.extension
    when (ext) {


        "svg" -> return SvgLoader().loadSvg(imagePathToLoad.toString())


        "png","jpg" -> return ImageView(Image(imagePathToLoad.inputStream()))

        else -> {
            println("ERROR")
            return ImageView(/*put a default image here*/)
        }
    }
}
