package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.paint.Paint
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class MinimapFragment: Fragment() {

    val vm: VerseMarkerViewModel by inject()
    val slider = slider {  }

    override val root = hbox {
        alignment = Pos.CENTER

        add(FontIcon("gmi-bookmark"))
        add(label().apply {
            textProperty().bind(vm.markerCountProperty.asString())
        })
        add(slider)
    }
}