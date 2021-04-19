package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class TitleFragment : Fragment() {

    val viewModel: VerseMarkerViewModel by inject()

    override val root = vbox {
        alignment = Pos.CENTER
        styleClass.add("vm-header")
        text {
            styleClass.add("vm-header__title")
            textProperty().bind(viewModel.headerTitle)
        }
        text {
            styleClass.add("vm-header__subtitle")
            textProperty().bind(viewModel.headerSubtitle)
        }
    }
}
