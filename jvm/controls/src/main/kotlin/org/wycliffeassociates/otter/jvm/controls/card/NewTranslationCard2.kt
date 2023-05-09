package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.model.TranslationMode
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import tornadofx.FX.Companion.messages
// TODO: remove number "2" suffix after deleting the original control.
class NewTranslationCard2(
    private val sourceLanguageProperty: ObservableValue<Language>,
    private val targetLanguageProperty: ObservableValue<Language>,
    mode: TranslationMode
) : VBox() {

    private var onCancel: () -> Unit = {}

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
                addClass("translation-card__language")
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
                addClass("translation-card__language")
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

            action {
                onCancel()
            }
        }
    }

    fun setOnCancelAction(op: () -> Unit) {
        onCancel = op
    }
}

class CreateTranslationCard : VBox() {

    private var onCreate: () -> Unit = {}

    init {
        addClass("create-translation-card")
        button {
            addClass("btn", "btn--primary")
            graphic = FontIcon(MaterialDesign.MDI_PLUS)
            action { onCreate() }
        }
    }

    fun setOnAction(op: () -> Unit) {
        onCreate = op
    }
}

fun EventTarget.newTranslationCard(
    sourceLanguage: ObservableValue<Language>,
    targetLanguage: ObservableValue<Language>,
    mode: TranslationMode,
    op: NewTranslationCard2.() -> Unit = {}
) = NewTranslationCard2(sourceLanguage, targetLanguage, mode).attachTo(this, op)
