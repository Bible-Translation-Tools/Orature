package org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.images.ImageLoader
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*
import tornadofx.WizardStyles.Companion.wizard


class ProjectWizardStyles : Stylesheet() {

    companion object {
        val wizardCard by cssclass()
        val wizardCardGraphicsContainer by cssclass()
        val collectionFlowPane by cssclass()
        val noResource by cssclass()
        val filterableComboBox by cssclass()
        val wizardButton by cssclass()
        val languageBoxLabel by cssclass()
        val selectLanguageRoot by cssclass()

        fun sourceLanguageIcon() = MaterialIconView(MaterialIcon.HEARING, "25px")
        fun targetLanguageIcon() = MaterialIconView(MaterialIcon.RECORD_VOICE_OVER, "25px")
        fun resourceGraphic(resourceSlug: String): Node {
            return when (resourceSlug) {
                SlugsEnum.ULB.slug -> MaterialIconView(MaterialIcon.BOOK, "50px")
                SlugsEnum.OBS.slug -> ImageLoader.load(
                        ClassLoader.getSystemResourceAsStream("images/obs.svg"),
                        ImageLoader.Format.SVG
                )
                SlugsEnum.TW.slug -> ImageLoader.load(
                        ClassLoader.getSystemResourceAsStream("images/tw.svg"),
                        ImageLoader.Format.SVG
                )
                SlugsEnum.OT.slug -> ImageLoader.load(
                        ClassLoader.getSystemResourceAsStream("images/old_testament.svg"),
                        ImageLoader.Format.SVG
                )
                SlugsEnum.NT.slug -> ImageLoader.load(
                        ClassLoader.getSystemResourceAsStream("images/cross.svg"),
                        ImageLoader.Format.SVG
                )
                else -> MaterialIconView(MaterialIcon.COLLECTIONS_BOOKMARK, "50px")
            }
        }
    }

    init {
        wizard {
            backgroundColor += AppTheme.colors.defaultBackground
        }

        selectLanguageRoot {
            alignment = Pos.CENTER
            spacing = 100.px
        }

        label {
            and(languageBoxLabel) {
                textFill = AppTheme.colors.defaultText
                child("*") {
                    fill = AppTheme.colors.defaultText
                }
            }
        }

        collectionFlowPane {
            vgap = 16.px
            hgap = 16.px
            alignment = Pos.CENTER
            padding = box(10.px)
        }

        wizardCard {
            prefWidth = 280.px
            prefHeight = 300.px
            backgroundColor += AppTheme.colors.cardBackground
            padding = box(10.px)
            backgroundRadius += box(10.px)
            spacing = 10.px
            wizardCardGraphicsContainer {
                backgroundRadius += box(10.px)
                backgroundColor += AppTheme.colors.imagePlaceholder
                child("*") {
                    fill = AppTheme.colors.defaultText
                }
            }
            label {
                textFill = AppTheme.colors.defaultText
                fontWeight = FontWeight.BOLD
                fontSize = 16.px
            }
            s(".jfx-button") {
                minHeight = 40.px
                maxWidth = Double.MAX_VALUE.px
                backgroundColor += AppTheme.colors.appRed
                textFill = AppTheme.colors.white
                cursor = Cursor.HAND
                fontSize = 16.px
                fontWeight = FontWeight.BOLD
            }
        }

        noResource {
            padding = box(50.px)
            backgroundColor += AppTheme.colors.defaultBackground
            fontSize = 24.px
            fontWeight = FontWeight.BOLD
            textFill = AppTheme.colors.defaultText
        }

        filterableComboBox {
            backgroundColor += Color.TRANSPARENT
            borderColor += box(null, null, AppTheme.colors.appRed, null)
            borderWidth += box(0.px, 0.px, 2.px, 0.px)
            minWidth = 350.px
            child(".text-input") {
                backgroundColor += Color.TRANSPARENT
                textFill = AppTheme.colors.defaultText
            }
            child(".arrow-button") {
                backgroundColor += Color.TRANSPARENT
                child(".arrow") {
                    visibility = FXVisibility.HIDDEN
                }
            }
            comboBoxPopup {
                listView {
                    backgroundColor += AppTheme.colors.defaultBackground
                    borderColor += box(AppTheme.colors.base)
                    listCell {
                        textFill = AppTheme.colors.defaultText
                        backgroundColor += AppTheme.colors.defaultBackground
                    }
                }
            }
        }

        wizardButton {
            prefHeight = 40.0.px
            prefWidth = 120.0.px
            backgroundColor += AppTheme.colors.appRed
            textFill = AppTheme.colors.white
            cursor = Cursor.HAND
        }
    }


}