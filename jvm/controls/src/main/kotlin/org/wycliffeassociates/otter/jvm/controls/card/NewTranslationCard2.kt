/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.jvm.controls.ethiopicFontLanguage
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.MessageFormat

// TODO: remove number "2" suffix after deleting the original control.
class NewTranslationCard2(
    private val sourceLanguageProperty: ObservableValue<Language>,
    private val targetLanguageProperty: ObservableValue<Language>,
    mode: ObservableValue<ProjectMode>
) : VBox() {

    private var onCancelProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("translation-card", "translation-card--active")

        hbox {
            addClass("translation-card__header")
            label {
                textProperty().bind(
                    mode.stringBinding {
                        it?.let {
                            MessageFormat.format(messages["newProjectWithMode"], messages[it.titleKey])
                        } ?: messages["newProject"]
                    }
                )
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
                        toggleClass("ethiopic-font", source?.slug in ethiopicFontLanguage)
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
                graphic = FontIcon(MaterialDesign.MDI_CHEVRON_DOUBLE_DOWN)
            }
            label {
                addClass("translation-card__language")
                textProperty().bind(
                    targetLanguageProperty.stringBinding { target ->
                        togglePseudoClass("unset", target == null)
                        toggleClass("ethiopic-font", target?.slug in ethiopicFontLanguage)
                        target?.name ?: "???"
                    }
                )
                graphic = FontIcon(MaterialDesign.MDI_VOICE)
            }
        }
        button(messages["cancel"]) {
            addClass("btn", "btn--secondary")
            tooltip(text)
            graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)

            onActionProperty().bind(onCancelProperty)
        }
    }

    fun setOnCancelAction(op: () -> Unit) = onCancelProperty.set { op() }
}

class TranslationCreationCard : HBox() {

    private var onCreateProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("translation-creation-card")
        vbox {
            addClass("translation-creation-card__box")
            prefWidth = 180.0
            rectangle(width = 140.0, height = 16.0).addClass("card-graphic")
            hbox {
                addClass("translation-creation-card__box")
                rectangle(width = 80.0, height = 16.0).addClass("card-graphic")
                rectangle(width = 15.0, height = 15.0).addClass("card-graphic")
                rectangle(width = 80.0, height = 16.0).addClass("card-graphic")
            }
        }
        region { hgrow = Priority.ALWAYS }
        button {
            addClass("btn", "btn--primary")
            tooltip(messages["newProject"])
            graphic = FontIcon(MaterialDesign.MDI_PLUS)

            onActionProperty().bind(onCreateProperty)
        }
    }

    fun setOnAction(op: () -> Unit) = onCreateProperty.set { op() }
}

fun EventTarget.newTranslationCard(
    sourceLanguage: ObservableValue<Language>,
    targetLanguage: ObservableValue<Language>,
    mode: ObservableValue<ProjectMode>,
    op: NewTranslationCard2.() -> Unit = {}
) = NewTranslationCard2(sourceLanguage, targetLanguage, mode).attachTo(this, op)

fun EventTarget.translationCreationCard(
    op: TranslationCreationCard.() -> Unit = {}
) = TranslationCreationCard().attachTo(this, op)

