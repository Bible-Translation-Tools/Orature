/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
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
