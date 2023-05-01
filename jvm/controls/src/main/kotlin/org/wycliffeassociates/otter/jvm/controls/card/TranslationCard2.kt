package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.model.TranslationMode
import tornadofx.FX
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.get
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.label
import java.text.MessageFormat

class TranslationCard2(
    sourceLanguage: String,
    targetLanguage: String,
    mode: TranslationMode
) : VBox() {

    private val cardTitleProperty = SimpleStringProperty(
        when (mode) {
            TranslationMode.TRANSLATION, TranslationMode.NARRATION -> {
                MessageFormat.format(FX.messages["translationMode"], FX.messages[mode.titleKey])
            }
        }
    )

    init {
        addClass("translation-card", "translation-card--selectable")

        hbox {
            addClass("translation-card__header")
            label(cardTitleProperty) {
                addClass("h5", "translation-card__header__text")
            }
        }
        hbox {
            addClass("translation-card__body")

            label(sourceLanguage) {
                addClass("translation-card__language", "label-normal")
            }
            hbox {
                hgrow = Priority.ALWAYS
                alignment = Pos.CENTER
                label {
                    addClass("translation-card__divider")
                    graphic = FontIcon(MaterialDesign.MDI_MENU_RIGHT)
                }
            }
            label(targetLanguage) {
                addClass("translation-card__language", "label-normal")
            }
        }
    }
}

fun EventTarget.translationCard(
    sourceLanguage: String,
    targetLanguage: String,
    mode: TranslationMode,
    op: TranslationCard2.() -> Unit = {}
) = TranslationCard2(sourceLanguage, targetLanguage, mode).attachTo(this, op)
