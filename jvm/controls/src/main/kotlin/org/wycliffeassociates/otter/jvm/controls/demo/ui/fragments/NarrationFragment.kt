package org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments

import tornadofx.*

class NarrationFragment : Fragment() {
    override val root = stackpane {
        vbox {
            label("Hello World") {
                addClass("wa--label")
            }
        }
    }
}