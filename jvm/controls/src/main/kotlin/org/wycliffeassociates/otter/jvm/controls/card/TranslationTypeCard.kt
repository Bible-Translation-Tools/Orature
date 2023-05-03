package org.wycliffeassociates.otter.jvm.controls.card

import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import tornadofx.FX.Companion.messages

class TranslationTypeCard(titleKey: String, descriptionKey: String) : HBox() {

    init {
        addClass("translation-type-card")
        vgrow = Priority.ALWAYS

        vbox {
            hgrow = Priority.SOMETIMES

            addClass("translation-type-card__text-cell")
            label(messages[titleKey]) {
                addClass("h3", "translation-type-card__text-cell__title")
            }
            label(messages[descriptionKey]) {
                addClass("label-normal")
                isWrapText = true
            }
        }
        vbox {
            hgrow = Priority.ALWAYS
            addClass("translation-type-card__action-cell")
            button(messages["select"]) {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                minWidth = Button.USE_PREF_SIZE
            }
        }
    }
}

fun EventTarget.translationTypeCard(
    titleKey: String,
    descriptionKey: String,
    op: TranslationTypeCard.() -> Unit = {}
) = TranslationTypeCard(titleKey, descriptionKey).attachTo(this, op)
