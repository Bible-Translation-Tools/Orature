package org.wycliffeassociates.otter.jvm.controls.card

import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Control
import javafx.scene.control.Separator
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import tornadofx.FX.Companion.messages

class TranslationTypeGrid : GridPane() {
    init {
        addClass("translation-type-grid")
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
        vgap = 10.0

        columnConstraints.setAll(
            ColumnConstraints().apply { },
            ColumnConstraints().apply { }
        )

        rowConstraints.setAll(
            RowConstraints().apply { percentHeight = 33.3 },
            RowConstraints().apply { percentHeight = 33.3 },
            RowConstraints().apply { percentHeight = 33.3 }
        )


        add(createTextCell("oralTranslation", "oralTranslationDesc"), 0, 0)
        add(createActionCell(), 1, 0)

        add(createTextCell("narration", "narrationDesc"), 0, 1)
        add(createActionCell(), 1, 1)

        add(createTextCell("dialect", "dialectDesc"), 0, 2)
        add(createActionCell(), 1, 2)

        add(Separator(Orientation.HORIZONTAL), 0, 0, 2, 2)
        add(Separator(Orientation.HORIZONTAL), 0, 1, 2, 2)

    }

    private fun createTextCell(
        titleKey: String,
        descriptionKey: String
    ): VBox {
        return VBox().apply {
            addClass("translation-type-card__text-cell")
            label(messages[titleKey]) {
                addClass("h3", "translation-type-card__text-cell__title")
            }
            label(descriptionKey) {
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. "
                addClass("label-normal")
                isWrapText = true
            }
        }
    }

    private fun createActionCell(): Node {
        return VBox().apply {
            addClass("translation-type-card__action-cell")
            button(messages["select"]) {
                text = "Lorem ipsum dolor"
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                minWidth = Button.USE_PREF_SIZE // don't allow button size below preferred size - show full text
            }
        }
    }
}

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
            label(descriptionKey) {
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. "
                addClass("label-normal")
                isWrapText = true
            }
        }
        vbox {
            hgrow = Priority.ALWAYS
            addClass("translation-type-card__action-cell")
            button(messages["select"]) {
                text = "Lorem ipsum dolor"
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

fun EventTarget.translationTypeGrid(
    op: TranslationTypeGrid.() -> Unit = {}
) = TranslationTypeGrid().attachTo(this, op)