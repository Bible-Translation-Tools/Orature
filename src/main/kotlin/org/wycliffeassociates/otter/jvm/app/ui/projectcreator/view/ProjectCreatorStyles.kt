package org.wycliffeassociates.otter.jvm.app.ui.projectcreator.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.images.ImageLoader
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.SlugsEnum
import org.wycliffeassociates.otter.jvm.app.widgets.searchablelist.SearchableListStyles
import tornadofx.*

class ProjectCreatorStyles : Stylesheet() {

    companion object {
        val wizardCard by cssclass()
        val wizardCardGraphicsContainer by cssclass()
        val collectionFlowPane by cssclass()
        val noResource by cssclass()
        val searchableList by cssclass()
        val wizardButton by cssclass()
        val sourceLanguageBoxLabel by cssclass()
        val targetLanguageBoxLabel by cssclass()
        val selectLanguageRoot by cssclass()
        val languageSearchContainer by cssclass()

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
        WizardStyles.wizard {
            backgroundColor += AppTheme.colors.defaultBackground
            prefWidth = 1000.px
            prefHeight = 800.px
        }

        selectLanguageRoot {
            alignment = Pos.CENTER
            spacing = 100.px
            padding = box(50.px, 0.px, 0.px, 0.px)
        }

        label {
            and(sourceLanguageBoxLabel) {
                textFill = AppTheme.colors.appBlue
                child("*") {
                    fill = AppTheme.colors.appBlue
                }
            }

            and(targetLanguageBoxLabel) {
                textFill = AppTheme.colors.appRed
                child("*") {
                    fill = AppTheme.colors.appRed
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

        languageSearchContainer {
            alignment = Pos.TOP_CENTER
            spacing = 10.px
        }

        searchableList {
            backgroundRadius += box(5.px)
            padding = box(10.px)
            minWidth = 350.px
            SearchableListStyles.searchFieldContainer {
                padding = box(5.px)
                backgroundColor += AppTheme.colors.base
            }
            SearchableListStyles.searchField {
                backgroundColor += AppTheme.colors.base
                // gets rid of a blue focus animation
                unsafe("-jfx-focus-color", raw(AppTheme.colors.appRed.css))
            }
            SearchableListStyles.searchListView {
                backgroundColor += AppTheme.colors.base
                borderColor += box(AppTheme.colors.base)
                Stylesheet.listCell {
                    Stylesheet.label {
                        textFill = AppTheme.colors.defaultText
                    }
                    backgroundColor += AppTheme.colors.base
                    backgroundRadius += box(5.px)
                    and(selected) {
                        backgroundColor += AppTheme.colors.appRed
                        Stylesheet.label {
                            textFill = AppTheme.colors.white
                        }
                    }
                }
            }
        }

        wizardButton {
            prefHeight = 40.0.px
            prefWidth = 150.0.px
            backgroundColor += AppTheme.colors.appRed
            textFill = AppTheme.colors.white
            cursor = Cursor.HAND
        }
    }
}