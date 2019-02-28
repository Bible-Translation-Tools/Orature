package org.wycliffeassociates.otter.jvm.app.ui.collectionsgrid.view

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class CollectionGridStyles: Stylesheet() {

    companion object {
        val collectionsContainer by cssclass()
        val contentLoadingProgress by cssclass()
    }

    init {
        collectionsContainer {
            vgap = 32.px
            hgap = 24.px
            horizontalCellSpacing = 24.0.px
            verticalCellSpacing = 32.0.px
            alignment = Pos.CENTER

        }

        contentLoadingProgress {
            progressColor = AppTheme.colors.appRed
        }
    }
}