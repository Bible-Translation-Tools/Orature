package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.model.TranslationMode
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.MessageFormat

class ActiveTranslationCard(
    sourceLanguage: String,
    targetLanguage: String,
    mode: TranslationMode
) : VBox() {

    val cardTitleProperty = SimpleStringProperty(
        when (mode) {
            TranslationMode.TRANSLATION, TranslationMode.NARRATION -> {
                MessageFormat.format(messages["translationMode"], messages[mode.titleKey])
            }
        }
    )

    init {
        addClass("translation-card")
        addPseudoClass("active")

        hbox {
            addClass("translation-card__header")
            label(cardTitleProperty) {
                addClass("h5", "translation-card__header__text")
            }
            region { hgrow = Priority.ALWAYS }
            label {
                graphic = FontIcon(MaterialDesign.MDI_INFORMATION_OUTLINE)
            }
        }
        vbox {
            addClass("translation-card__body")
            label(sourceLanguage) {
                addClass("translation-card__language", "label-normal")
                graphic = FontIcon(Material.HEARING)
            }
            label {
                addClass("translation-card__divider")
                graphic = FontIcon(MaterialDesign.MDI_MENU_DOWN)
            }
            label(targetLanguage) {
                addClass("translation-card__language", "label-normal")
                graphic = FontIcon(MaterialDesign.MDI_VOICE)
            }
        }
    }
}

fun EventTarget.activeTranslationCard(
    sourceLanguage: String,
    targetLanguage: String,
    mode: TranslationMode,
    op: ActiveTranslationCard.() -> Unit = {}
) = ActiveTranslationCard(sourceLanguage, targetLanguage, mode).attachTo(this, op)
