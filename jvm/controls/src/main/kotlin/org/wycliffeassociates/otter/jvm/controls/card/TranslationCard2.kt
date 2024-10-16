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

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.ButtonBase
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.jvm.controls.model.ProjectGroupKey
import java.text.MessageFormat
import tornadofx.*
import tornadofx.FX.Companion.messages

// TODO: remove number "2" suffix after deleting the original control. Same for css named translation-card-2.css
class TranslationCard2(
    private val sourceLanguage: Language,
    private val targetLanguage: Language,
    private val mode: ProjectMode,
    private val resourceSlug: String,
    selectedProjectGroupProperty: ObservableValue<ProjectGroupKey>
) : ButtonBase() {

    val cardTitleProperty = SimpleStringProperty(
        MessageFormat.format(messages["projectModeTitle"], resourceSlug.uppercase(), messages[mode.titleKey])
    )
    val sourceLanguageProperty = SimpleObjectProperty(sourceLanguage)
    val targetLanguageProperty = SimpleObjectProperty(targetLanguage)
    val cardInfo: String = when (mode) {
        ProjectMode.TRANSLATION -> messages["oralTranslationDesc"]
        ProjectMode.NARRATION -> messages["narrationDesc"]
        ProjectMode.DIALECT -> messages["dialectDesc"]
    }

    init {
        addClass("translation-card-button")
        skinProperty().bind(
            selectedProjectGroupProperty.objectBinding { selectedGroup ->
                // if the selected card is this card, displays the active skin
                if (selectedGroup == this.getKey()) {
                    isFocusTraversable = false
                    removePseudoClass("focused")
                    ActiveTranslationCardSkin(this)
                } else {
                    isFocusTraversable = true
                    TranslationCardSkin2(this)
                }
            }
        )
    }

    override fun createDefaultSkin(): Skin<*> {
        return TranslationCardSkin2(this)
    }

    override fun fire() {
        if (!isDisabled) {
            fireEvent(ActionEvent())
        }
    }

    private fun getKey() = ProjectGroupKey(sourceLanguage.slug, targetLanguage.slug, resourceSlug, mode)
}

class TranslationCardSkin2(card: TranslationCard2) : SkinBase<TranslationCard2>(card) {
    private val behavior = ButtonBehavior(card)

    private val sourceLanguageProperty = SimpleStringProperty()
    private val targetLanguageProperty = SimpleStringProperty()

    private val graphic = VBox().apply {
        addClass("translation-card", "translation-card--selectable")

        hbox {
            addClass("translation-card__header")
            label(card.cardTitleProperty) {
                addClass("h5", "translation-card__header__text")
            }
        }
        hbox {
            addClass("translation-card__body")

            label(sourceLanguageProperty) {
                addClass("translation-card__language")
            }
            hbox {
                hgrow = Priority.ALWAYS
                alignment = Pos.CENTER
                label {
                    addClass("translation-card__divider")
                    graphic = FontIcon(MaterialDesign.MDI_CHEVRON_DOUBLE_RIGHT)
                }
            }
            label(targetLanguageProperty) {
                addClass("translation-card__language")
            }
        }
    }

    init {
        sourceLanguageProperty.bind(card.sourceLanguageProperty.stringBinding { it?.slug })
        targetLanguageProperty.bind(card.targetLanguageProperty.stringBinding { it?.slug })

        children.setAll(graphic)
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}

class ActiveTranslationCardSkin(card: TranslationCard2) : SkinBase<TranslationCard2>(card) {

    private val sourceLanguageProperty = SimpleStringProperty()
    private val targetLanguageProperty = SimpleStringProperty()

    private val graphic = VBox().apply {
        addClass("translation-card", "translation-card--active")

        hbox {
            addClass("translation-card__header")
            label(card.cardTitleProperty) {
                addClass("h5", "translation-card__header__text")
            }
            region { hgrow = Priority.ALWAYS }
            label {
                graphic = FontIcon(MaterialDesign.MDI_INFORMATION_OUTLINE)
                tooltip(card.cardInfo)
            }
        }
        vbox {
            addClass("translation-card__body")
            vbox {
                label(messages["sourceLanguage"]) {
                    addClass("h5", "translation-card__subtitle")
                }
                label(sourceLanguageProperty) {
                    addClass("translation-card__language")
                    graphic = FontIcon(Material.HEARING)
                }
            }
            separator()
            vbox {
                label(messages["targetLanguage"]) {
                    addClass("h5", "translation-card__subtitle")
                }
                label(targetLanguageProperty) {
                    addClass("translation-card__language")
                    graphic = FontIcon(MaterialDesign.MDI_VOICE)
                }
            }
        }
    }

    init {
        sourceLanguageProperty.bind(card.sourceLanguageProperty.stringBinding { it?.name })
        targetLanguageProperty.bind(card.targetLanguageProperty.stringBinding { it?.name })

        children.setAll(graphic)
    }
}

fun EventTarget.translationCard(
    sourceLanguage: Language,
    targetLanguage: Language,
    mode: ProjectMode,
    resourceSlug: String,
    selectedCardProperty: ObjectProperty<ProjectGroupKey> = SimpleObjectProperty(),
    op: TranslationCard2.() -> Unit = {}
) = TranslationCard2(sourceLanguage, targetLanguage, mode, resourceSlug, selectedCardProperty).attachTo(this, op)
