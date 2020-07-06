package org.wycliffeassociates.otter.jvm.utils.images

import afester.javafx.svg.SvgLoader
import java.io.InputStream
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView

// Loads an image with a given file path
class ImageLoader {
    enum class Format {
        SVG,
        PNG,
        JPG
    }

    companion object {
        fun load(imageStream: InputStream, format: Format): Node {
            return when (format) {
                Format.SVG -> SVGImage(SvgLoader().loadSvg(imageStream))
                Format.PNG, Format.JPG -> ImageView(Image(imageStream))
            }
        }
    }
}
