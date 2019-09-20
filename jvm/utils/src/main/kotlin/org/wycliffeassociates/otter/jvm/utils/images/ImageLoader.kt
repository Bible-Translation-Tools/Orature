package org.wycliffeassociates.otter.jvm.utils.images

import afester.javafx.svg.SvgLoader
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.io.InputStream

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
