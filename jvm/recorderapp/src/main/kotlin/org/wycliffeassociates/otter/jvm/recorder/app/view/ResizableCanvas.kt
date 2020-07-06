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