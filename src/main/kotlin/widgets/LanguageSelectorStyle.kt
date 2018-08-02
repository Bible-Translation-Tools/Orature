package widgets

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

/**
 * This class is the style sheet for LanguageSelector and for the chips of the selected items.
 *
 * It adds the Chip and Rectangle objects by csselement() so that those objects can be selected and styled en masse.
 * The Stylesheet handles all styling other than color, which is currently handled in the LanguageSelector.
 *
 * @author Caleb Benedick and Kimberly Horton
 */
class LanguageSelectorStyle : Stylesheet() {

    companion object {
        val filterableComboBox by csselement("FilterableComboBox")
        val chip by csselement("Chip")
        val rectangle by csselement("Rectangle")
        val comboBoxLabel by cssid("comboBoxLabel")
        val labelIconHBox by cssid("labelIconHBox")
        val mainLabel by cssid("mainLabel")
        val subLabel by cssid("subLabel")
    }

    init {
        s(filterableComboBox) {
            faintFocusColor = Color.TRANSPARENT

            s(arrowButton) {
                backgroundColor = multi(Color.TRANSPARENT)
            }

            s(listView) {
                maxHeight = 125.px
            }
        }

        s(chip) {
            alignment = Pos.CENTER_LEFT
            prefHeight = 25.px

            s(rectangle) {
                arcWidth = 30.px
                arcHeight = 30.px
                prefHeight = 25.px
            }

            s(button) {
                backgroundColor = multi(Color.TRANSPARENT)
                opacity = 0.65
                alignment = Pos.CENTER_RIGHT
                padding = box(10.px, 10.px, 5.px, 10.px)

                and(hover) {
                    opacity = 1.0
                }
            }

            s(label) {
                alignment = Pos.CENTER_LEFT
            }
        }

        comboBoxLabel {
            alignment = Pos.CENTER_RIGHT
            fontWeight = FontWeight.BOLD
        }

        labelIconHBox {
            alignment = Pos.CENTER
        }

        mainLabel {
            fontWeight = FontWeight.BOLD
            padding = box(10.px, 0.px, 10.px, 10.px) // top, right, bottom, left
        }

        subLabel {
            padding = box(9.5.px, 0.px, 10.px, 3.px) // top, right, bottom, left
        }
    }
}
