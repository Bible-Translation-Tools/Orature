package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.model.TranslationMode
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import tornadofx.FX.Companion.messages

class NewTranslationCard2(
    val sourceLanguageProperty: SimpleObjectProperty<Language>,
    val targetLanguageProperty: SimpleObjectProperty<Language>,
    mode: TranslationMode
) : VBox() {

    init {
        addClass("translation-card")
        addPseudoClass("active")

        hbox {
            addClass("translation-card__header")
            label(messages["newProject"]) {
                addClass("h5", "translation-card__header__text")
            }
        }
        vbox {
            addClass("translation-card__body")
            label {
                addClass("translation-card__language", "label-normal")
                textProperty().bind(
                    sourceLanguageProperty.stringBinding { source ->
                        togglePseudoClass("unset", source == null)
                        source?.name ?: "???"
                    }
                )
                graphic = FontIcon(Material.HEARING)
            }
            label {
                addClass("translation-card__divider")
                sourceLanguageProperty.onChangeAndDoNow { source ->
                    togglePseudoClass("unset", source == null)
                }
                graphic = FontIcon(MaterialDesign.MDI_MENU_DOWN)
            }
            label {
                addClass("translation-card__language", "label-normal")
                textProperty().bind(
                    targetLanguageProperty.stringBinding { target ->
                        togglePseudoClass("unset", target == null)
                        target?.name ?: "???"
                    }
                )
                graphic = FontIcon(MaterialDesign.MDI_VOICE)
            }
        }
        button(messages["cancel"]) {
            addClass("btn", "btn--secondary")
            graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
        }
    }

}