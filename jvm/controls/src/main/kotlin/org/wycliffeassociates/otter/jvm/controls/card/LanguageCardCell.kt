package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.layout.HBox
import tornadofx.*

class LanguageCardCell : HBox() {

    val iconProperty = SimpleObjectProperty<Node>()
    val languageNameProperty = SimpleStringProperty()
    val languageSlugProperty = SimpleStringProperty()

    init {
        importStylesheet(javaClass.getResource("/css/language-card-cell.css").toExternalForm())
        styleClass.setAll("language-card-cell")

        label {
            addClass("language-card-cell__icon")
            graphicProperty().bind(iconProperty)
        }

        vbox {
            addClass("language-card-cell__title")
            label(languageNameProperty).apply {
                addClass("language-card-cell__name")
            }
            label(languageSlugProperty).apply {
                addClass("language-card-cell__slug")
            }
        }
    }
}
