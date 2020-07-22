package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*
import tornadofx.FX.Companion.stylesheets

class TitleFragment : Fragment() {

    val vm: VerseMarkerViewModel by inject()

    override val root = vbox {
        alignment = Pos.CENTER
        styleClass.add("vm-header")
        text {
            styleClass.add("vm-header__title")
            textProperty().bind(vm.headerTitle)
        }
        text {
            styleClass.add("vm-header__subtitle")
            textProperty().bind(vm.headerSubtitle)
        }
    }
}
