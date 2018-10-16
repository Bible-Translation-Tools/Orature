package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ContentDisplay
import org.wycliffeassociates.otter.jvm.app.ui.imageLoader
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.SlugsEnum
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import org.wycliffeassociates.otter.jvm.app.ui.styles.ProjectWizardStyles
import tornadofx.*
import java.io.File

class SelectChildren: View() {

    val viewModel: ProjectCreationViewModel by inject()
    override val complete = viewModel.resourceSelected
    override val root =  flowpane {
        alignment = Pos.CENTER
        togglegroup {

//            viewModel.goNextPage.onChange {
//                if (true) {
                    viewModel.collectionList.forEach {
                        togglebutton {
                            isSelected = false //no initial selection
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
                                //if (isSelected) viewModel.selectedResourceProperty.value = it
                                viewModel.checkLevel(it)
                            }
                        }
                    }
                //}
            //}
        }
    }

    private fun resourceGraphic(resourceSlug: String): Node {

        return when (resourceSlug) {
            SlugsEnum.ULB.slug -> MaterialIconView(MaterialIcon.BOOK)
            SlugsEnum.OBS.slug -> imageLoader(File(ClassLoader.getSystemResource("assets/OBS.svg").toURI()))
            SlugsEnum.TW.slug -> imageLoader(File(ClassLoader.getSystemResource("assets/tW.svg").toURI()))

            else -> MaterialIconView(MaterialIcon.COLLECTIONS_BOOKMARK)
        }
    }

//    override fun onSave() {
//        viewModel.getResourceChildren()
//    }

}