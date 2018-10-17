package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments


import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ContentDisplay
import org.wycliffeassociates.otter.jvm.app.ui.imageLoader
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.SlugsEnum.*
import org.wycliffeassociates.otter.jvm.app.ui.styles.ProjectWizardStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import tornadofx.*
import java.io.File

class SelectAnthology : View() {
    val viewModel: ProjectCreationViewModel by inject()
//    override val complete = viewModel.anthologySelected

    init {
        importStylesheet<ProjectWizardStyles>()
    }
    override val root = hbox(40) {
        alignment = Pos.CENTER
        togglegroup {

            viewModel.anthologyList.onChange {
                clear() //clear to ensure that we create duplicate anthologies
                viewModel.anthologyList.forEach {
                    togglebutton {
                        isSelected = false
                        contentDisplay = ContentDisplay.TOP
                        graphic = resourceGraphic(it.slug)

                        if (isSelected) {
                            addClass(ProjectWizardStyles.selectedCard)
                        } else {
                            addClass(ProjectWizardStyles.unselectedCard)
                        }
                        text = it.titleKey
                        alignment = Pos.CENTER

                        selectedProperty().onChange {
                            if (it) {
                                removeClass(ProjectWizardStyles.unselectedCard)
                                addClass(ProjectWizardStyles.selectedCard)
                            } else {
                                removeClass(ProjectWizardStyles.selectedCard)
                                addClass(ProjectWizardStyles.unselectedCard)
                            }
                        }
                        action {
//                            viewModel.selectedAnthologyProperty.value = it
                        }
                    }
                }
            }
        }
    }

    private fun resourceGraphic(resourceSlug: String): Node {

        return when (resourceSlug) {
            OT.slug ->  imageLoader(File(ClassLoader.getSystemResource("assets/Old_Testament.svg").toURI()))
            NT.slug ->  imageLoader(File(ClassLoader.getSystemResource("assets/Cross.svg").toURI()))

            else -> MaterialIconView(MaterialIcon.COLLECTIONS_BOOKMARK)
        }
    }

    override fun onSave() {
//        viewModel.getBooks()
    }
}
