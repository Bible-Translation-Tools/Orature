package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments

import tornadofx.*
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.ObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventType
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.ToggleGroup
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import org.omg.CORBA.Object
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.WizardCard
import org.wycliffeassociates.otter.jvm.app.widgets.wizardcard
import javax.annotation.Resource

class SelectResource : View() {
    val viewModel: ProjectCreationViewModel by inject()
    val toggleGroup = ToggleGroup()
//    override val complete = viewmodel.valid(viewmodel.resource)
    override val root =  hbox(40) {
                alignment = Pos.CENTER
//                wizardcard {
//                    text = "Bible"
//                    selected = true
//                    // image = imageLoader(File("/Users/NathanShanko/Downloads/OBS.svg"))
//                }
                togglegroup {

                    addEventHandler(ActionEvent.ACTION) {
                    }

                    togglebutton {
                        contentDisplay = ContentDisplay.TOP
                        graphic = MaterialIconView(MaterialIcon.COLLECTIONS_BOOKMARK)
                        if (isSelected) {
                            addClass(ResourceStyles.selectedCard)
                        } else {
                            addClass(ResourceStyles.unselectedCard)
                        }
                        text = messages["bible"]
                        alignment = Pos.CENTER

                        selectedProperty().onChange {
                            if (it) {
                                removeClass(ResourceStyles.unselectedCard)
                                addClass(ResourceStyles.selectedCard)
                            } else {
                                removeClass(ResourceStyles.selectedCard)
                                addClass(ResourceStyles.unselectedCard)
                            }
                        }

                    }

                    togglebutton {
                        contentDisplay = ContentDisplay.TOP
                        //graphic = imageLoader(File("/Users/NathanShanko/Downloads/OBS.svg"))
                        if (isSelected) {
                            addClass(ResourceStyles.selectedCard)
                        } else {
                            addClass(ResourceStyles.unselectedCard)
                        }
                        text = messages["obs"]
                        alignment = Pos.CENTER
                        selectedProperty().onChange {
                            if (it) {
                                removeClass(ResourceStyles.unselectedCard)
                                addClass(ResourceStyles.selectedCard)
                            } else {
                                removeClass(ResourceStyles.selectedCard)
                                addClass(ResourceStyles.unselectedCard)
                            }
                        }

                    }

                    togglebutton {
                        contentDisplay = ContentDisplay.TOP
                        //graphic = imageLoader(File("/Users/NathanShanko/Downloads/tW.svg"))
                        if (isSelected) {
                            addClass(ResourceStyles.selectedCard)
                        } else {
                            addClass(ResourceStyles.unselectedCard)
                        }
                        text = messages["other"]
                        alignment = Pos.CENTER

                        selectedProperty().onChange {
                            if (it) {
                                removeClass(ResourceStyles.unselectedCard)
                                addClass(ResourceStyles.selectedCard)
                            } else {
                                removeClass(ResourceStyles.selectedCard)
                                addClass(ResourceStyles.unselectedCard)
                            }
                        }

                    }
                }
            }

    init {
        importStylesheet<ResourceStyles>()
    }

    private fun WizardCard.select() {

    }
}

class ResourceStyles : Stylesheet() {

    companion object {
        val selectedCard by cssclass()
        val unselectedCard by cssclass()
    }

    init {
        selectedCard {
            prefHeight = 364.0.px
            prefWidth = 364.0.px
            backgroundRadius += box(12.0.px)
            backgroundColor += c(Colors["primary"])
            textFill = c(Colors["base"])
            fontSize = 24.px
            effect = DropShadow(10.0, Color.GRAY)
            cursor = Cursor.HAND
        }

        unselectedCard {
            prefHeight = 364.0.px
            prefWidth = 364.0.px
            backgroundRadius += box(12.0.px)
            backgroundColor += c(Colors["base"])
            textFill = c(Colors["primary"])
            fontSize = 24.px
            effect = DropShadow(10.0, Color.GRAY)
            cursor = Cursor.HAND
        }
    }

}