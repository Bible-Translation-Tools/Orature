/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import javafx.scene.Group
import javafx.scene.layout.StackPane
import tornadofx.add
import tornadofx.doubleBinding
import tornadofx.getProperty
import tornadofx.property

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
                        scaleY = (width / svgAspectRatio) / svgGroup.boundsInLocal.height
                    }
                    return@doubleBinding scaleY
                }
        )
        minHeight = 0.0
        minWidth = 0.0
        add(svgGroup)
    }
}
