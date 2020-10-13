package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.jfoenix.controls.JFXButton
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class PlaceMarkerLayer : Fragment() {

    val viewModel: VerseMarkerViewModel by inject()

    override val root = vbox {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        alignment = Pos.BOTTOM_CENTER

        add(
            JFXButton("", FontIcon("mdi-bookmark-plus-outline")).apply {
                styleClass.addAll(
                    "btn--cta",
                    "vm-play-controls__btn--rounded",
                    "vm-play-controls__add-marker-btn"
                )
                setOnAction {
                    viewModel.placeMarker()
                }
            }
        )
        style {
            styleClass.addAll("vm-play-controls__add-marker-container")
        }
    }
}
