package org.wycliffeassociates.otter.jvm.app.ui

import javafx.scene.Node
import afester.javafx.svg.SvgLoader
import javafx.scene.Group
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import tornadofx.add
import tornadofx.doubleBinding
import tornadofx.getProperty
import tornadofx.property
import java.io.InputStream

class SVGImage(svgGroup: Group) : StackPane() {
    private val svgAspectRatio = svgGroup.boundsInLocal.width / svgGroup.boundsInLocal.height
    var preserveAspect: Boolean by property(true)
    fun preserveAspectProperty() = getProperty(SVGImage::preserveAspect)
    init {
        // Setup bindings so svg scales to fit Node
        svgGroup.scaleXProperty().bind(
                widthProperty().doubleBinding(heightProperty(), preserveAspectProperty()) {
                    var scaleX = (it?.toDouble() ?: 0.0) / svgGroup.boundsInLocal.width
                    if (preserveAspect && width / height > svgAspectRatio) {
                        // Wider than it should be
                        scaleX = svgAspectRatio * height / svgGroup.boundsInLocal.width
                    }
                    return@doubleBinding scaleX
                }
        )
        svgGroup.scaleYProperty().bind(
                heightProperty().doubleBinding(widthProperty(), preserveAspectProperty()) {
                    var scaleY = (it?.toDouble() ?: 0.0) / svgGroup.boundsInLocal.height
                    if (preserveAspect && width / height < svgAspectRatio) {
                        // Taller than it should be
                        scaleY =  (width / svgAspectRatio) / svgGroup.boundsInLocal.height
                    }
                    return@doubleBinding scaleY
                }
        )
        minHeight = 0.0
        minWidth = 0.0
        add(svgGroup)
    }
}

//Loads an image with a given file path
//How to use is described below
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

