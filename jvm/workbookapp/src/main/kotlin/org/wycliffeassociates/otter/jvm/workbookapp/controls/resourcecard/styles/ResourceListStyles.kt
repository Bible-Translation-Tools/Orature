package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.styles

import javafx.scene.paint.Color
import tornadofx.*

typealias LinearU = Dimension<Dimension.LinearUnits>

class ResourceListStyles : Stylesheet() {

    companion object {
        val resourceGroupList by cssclass()
    }

    init {
        resourceGroupList {
            borderColor += box(Color.TRANSPARENT) // Necessary for border under status bar banner to stay visible
            padding = box(0.px, 0.px, 0.px, 80.px) // Left "margin"
            scrollBar {
                +margin(0.px, 0.px, 0.px, 80.px) // Margin between scrollbar and right side of cards
            }

            listCell {
                // Add space between the cards (top margin)
                // But need to make the "margin" at least as large as the dropshadow offsets
                +margin(30.px, 4.px, 0.px, 0.px)
            }
        }
    }

    private fun margin(top: LinearU, right: LinearU, bottom: LinearU, left: LinearU) = mixin {
        padding = box(top, right, bottom, left)
        backgroundInsets += box(top, right, bottom, left)
    }
}
