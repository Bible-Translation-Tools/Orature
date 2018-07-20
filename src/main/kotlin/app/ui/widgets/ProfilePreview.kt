package app.ui.widgets

import app.ui.styles.ButtonStyles
import app.ui.widgets.profileIcon.ProfileIcon
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import tornadofx.*

class ProfilePreview(hash: String): HBox() {

    val NewUserButton = ProfileIcon(hash, 152.0)

    val micIcon = MaterialIconView(MaterialIcon.MIC_NONE, "25px")
    val rightArrow = MaterialIconView(MaterialIcon.ARROW_FORWARD, "25px")


    init {
        spacing = 48.0
        alignment= Pos.CENTER

        vbox {
            micIcon.fill = c("#CC4141")
            spacing = 12.0
            alignment= Pos.CENTER
            stackpane {
                circle {

                    style{
                        radius= 55.0
                        fill = c("#E5E5E5")
                    }
                }

                button("",micIcon) {
                    importStylesheet(ButtonStyles::class)
                    addClass(ButtonStyles.roundButton)
                    style {

                        backgroundColor += Color.WHITE
                        cursor = Cursor.HAND
                        minWidth = 75.0.px
                        minHeight = 75.0.px
                        fontSize = 2.em
                        textFill = c("#CC4141")
                    }
                }
            }

            label("REDO")
        }

        stackpane {
            circle {

                style{
                    radius= 120.0
                    fill = c("#E5E5E5")
                }

            }

        add(NewUserButton)
        }


        vbox {
            spacing = 12.0
            alignment= Pos.CENTER
            rightArrow.fill = c("#FFFFFF")
            stackpane {
                circle {

                    style{
                        radius= 55.0
                        fill = c("#E5E5E5")
                    }

                }
                button("", rightArrow) {
                    importStylesheet(ButtonStyles::class)
                    addClass(ButtonStyles.roundButton)
                    style {
                        backgroundColor += c("#CC4141")
                        cursor = Cursor.HAND
                        minWidth = 75.0.px
                        minHeight = 75.0.px
                        fontSize = 2.em
                        textFill = c("#CC4141")
                    }
                }
            }

            label("NEXT")
        }

    }

}