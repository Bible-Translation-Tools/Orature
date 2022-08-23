package org.wycliffeassociates.otter.jvm.controls.card

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import tornadofx.*

class LanguageCardCellSkin(private val cell: LanguageCardCell) : SkinBase<LanguageCardCell>(cell) {
    private val behavior = ButtonBehavior(cell)

    init {
        children.addAll(
            HBox().apply {
                addClass("language-card-cell__root")

                label {
                    addClass("language-card-cell__icon")
                    graphicProperty().bind(cell.iconProperty)
                }

                vbox {
                    addClass("language-card-cell__title")
                    label(cell.languageNameProperty).apply {
                        addClass("language-card-cell__name")
                    }
                    label(cell.languageSlugProperty).apply {
                        addClass("language-card-cell__slug")
                    }
                }
            }
        )
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}
