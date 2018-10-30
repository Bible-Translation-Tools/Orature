package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import org.wycliffeassociates.otter.jvm.app.ui.imageLoader
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.SlugsEnum
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import org.wycliffeassociates.otter.jvm.app.ui.styles.AppStyles
import org.wycliffeassociates.otter.jvm.app.widgets.wizardcard
import tornadofx.*
import java.io.File

class SelectCollection : View() {

    private val viewModel: ProjectCreationViewModel by inject()
    override val root = borderpane {
        center {
            scrollpane {
                isFitToHeight = true
                isFitToWidth = true
                flowpane {
                    vgap = 16.0
                    hgap = 16.0
                    alignment = Pos.CENTER
                    padding = Insets(10.0)
                    bindChildren(viewModel.collectionList) {
                        hbox {
                            wizardcard {
                                addClass(AppStyles.wizardCard)
                                text = it.titleKey
                                buttonText = messages["select"]
                                cardButton.apply {
                                    text = messages["select"]
                                    action {
                                        viewModel.doOnUserSelection(it)
                                    }
                                }
                                graphicContainer.apply {
                                    addClass(AppStyles.wizardCardGraphicsContainer)
                                    add(resourceGraphic(it.slug).apply {
                                        minWidth = 50.0
                                        minHeight = 50.0
                                    })
                                }
                            }
                        }
                    }
                    hbox {
                        if (viewModel.collectionList.isEmpty()) { //if user selects resource with no children initially
                            label(messages["noResources"]) {
                                addClass(AppStyles.noResource)
                            }
                        }
                        viewModel.collectionList.onChange {
                            clear()
                            if (viewModel.collectionList.isEmpty()) {
                                label(messages["noResources"]) {
                                    addClass(AppStyles.noResource)
                                }
                            }
                        }
                    }
                }
            }

        }

    }

    private fun resourceGraphic(resourceSlug: String): Node {
        return when (resourceSlug) {
            SlugsEnum.ULB.slug -> MaterialIconView(MaterialIcon.BOOK, "50px")
            SlugsEnum.OBS.slug -> imageLoader(File(ClassLoader.getSystemResource("assets/OBS.svg").toURI()))
            SlugsEnum.TW.slug -> imageLoader(File(ClassLoader.getSystemResource("assets/tW.svg").toURI()))
            SlugsEnum.OT.slug -> imageLoader(File(ClassLoader.getSystemResource("assets/Old_Testament.svg").toURI()))
            SlugsEnum.NT.slug -> imageLoader(File(ClassLoader.getSystemResource("assets/Cross.svg").toURI()))
            else -> MaterialIconView(MaterialIcon.COLLECTIONS_BOOKMARK, "50px")
        }
    }
}


