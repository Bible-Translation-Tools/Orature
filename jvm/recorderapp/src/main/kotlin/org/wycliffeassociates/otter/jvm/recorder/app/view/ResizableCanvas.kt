/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.scene.canvas.Canvas

class ResizableCanvas : Canvas() {

    override fun isResizable(): Boolean {
        return true
    }

    override fun maxHeight(width: Double): Double {
        return Double.POSITIVE_INFINITY
    }

    override fun maxWidth(height: Double): Double {
        return Double.POSITIVE_INFINITY
    }

    override fun minWidth(height: Double): Double {
        return 0.0
    }

    override fun minHeight(width: Double): Double {
        return 0.0
    }

    override fun resize(width: Double, height: Double) {
        this.width = width
        this.height = height
    }
}