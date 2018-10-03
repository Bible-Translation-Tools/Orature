package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments


import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.layout.HBox
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.imageLoader
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import tornadofx.*
import tornadofx.Stylesheet.Companion.root
import java.io.File

class SelectAnthology : View() {
    val viewModel: ProjectCreationViewModel by inject()
    override val root = hbox(40) {
        alignment = Pos.CENTER
        togglegroup {

            addEventHandler(ActionEvent.ACTION) {
            }

            togglebutton {
                contentDisplay = ContentDisplay.TOP
                //graphic = imageLoader(File("/Users/nathanshanko/Downloads/Old Testament.svg"))
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
                //graphic = imageLoader(File("/Users/nathanshanko/Downloads/Cross.svg"))
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
        }

    }
}
