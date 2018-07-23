package app.ui
import javafx.scene.Node
import afester.javafx.svg.SvgLoader
import javafx.scene.image.Image
import javafx.scene.image.ImageView

import java.io.File

fun blablabla(fileMine: File): Node {
    val ext: String = fileMine.extension
    when (ext) {
        "svg" -> {
            return SvgLoader().loadSvg(fileMine.toString())
        };
        "png","jpg" -> {
            val image = Image(fileMine.inputStream())
            val retImageView = ImageView(image)
            return retImageView
        };
        else -> {
            println("ERROR")
            return ImageView(/*put a default image here*/)
        }
    }
}
