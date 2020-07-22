package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class MinimapFragment : Fragment() {

    val vm: VerseMarkerViewModel by inject()

    val slider = slider { }

    override val root = hbox {
        alignment = Pos.CENTER_LEFT
        styleClass.add("vm-minimap-container")
        hbox {
            alignment = Pos.CENTER_LEFT
            spacing = 10.0
            button {
                graphic = FontIcon("gmi-bookmark")
                styleClass.add("vm-marker-count__icon")
            }
            add(label().apply {
                textProperty().bind(vm.markerRatioProperty)
            })
        }
        add(slider.apply {
            hgrow = Priority.ALWAYS
        })
    }
}
