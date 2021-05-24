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
        importStylesheet(javaClass.getResource("/css/language-card.css").toExternalForm())
        styleClass.setAll("language-card")

        label {
            addClass("language-card__icon")
            graphicProperty().bind(iconProperty)
        }

        vbox {
            addClass("language-card__title")
            label(languageNameProperty).apply {
                addClass("language-card__name")
            }
            label(languageSlugProperty).apply {
                addClass("language-card__slug")
            }
        }
    }
}
