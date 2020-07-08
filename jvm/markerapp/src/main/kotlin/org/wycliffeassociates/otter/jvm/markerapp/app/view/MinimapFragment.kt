package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.paint.Paint
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class MinimapFragment: Fragment() {

    val slider = slider {  }

    override val root = hbox {
        alignment = Pos.CENTER

        add(FontIcon("gmi-bookmark"))
        add(label("0/21"))
        add(slider)
    }
}