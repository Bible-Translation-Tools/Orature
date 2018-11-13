package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ProjectCollection
import org.wycliffeassociates.otter.jvm.app.ui.ImageLoader
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.SlugsEnum
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import org.wycliffeassociates.otter.jvm.app.ui.styles.AppStyles
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog
import org.wycliffeassociates.otter.jvm.app.widgets.wizardcard
import tornadofx.*

class SelectCollection : View() {
    private val viewModel: ProjectCreationViewModel by inject()
    override val root = stackpane {
        borderpane {
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
                                    var projectExists = false
                                    if (it.labelKey == "book") { //only check if project exists when we are at book level
                                        projectExists = doesProjectExist(viewModel.selectedLanguageProjects.value, it)
                                    }
                                    addClass(AppStyles.wizardCard)
                                    text = it.titleKey
                                    buttonText = messages["select"]
                                    cardButton.apply {
                                        text = messages["select"]
                                        action {
                                            viewModel.doOnUserSelection(it)
                                        }
                                        isDisable = projectExists
                                    }
                                    graphicContainer.apply {
                                        addClass(AppStyles.wizardCardGraphicsContainer)
                                        add(resourceGraphic(it.slug))
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

        val dialog = progressdialog {
            text = messages["pleaseWaitCreatingProject"]
        }
        viewModel.showOverlayProperty.onChange { it: Boolean ->
            Platform.runLater { if (it) dialog.open() else dialog.close() }
        }
    }


    private fun resourceGraphic(resourceSlug: String): Node {
        return when (resourceSlug) {
            SlugsEnum.ULB.slug -> MaterialIconView(MaterialIcon.BOOK, "50px")
            SlugsEnum.OBS.slug -> ImageLoader.load(
                    ClassLoader.getSystemResourceAsStream("assets/OBS.svg"),
                    ImageLoader.Format.SVG
            )
            SlugsEnum.TW.slug -> ImageLoader.load(
                    ClassLoader.getSystemResourceAsStream("assets/tW.svg"),
                    ImageLoader.Format.SVG
            )
            SlugsEnum.OT.slug -> ImageLoader.load(
                    ClassLoader.getSystemResourceAsStream("assets/Old_Testament.svg"),
                    ImageLoader.Format.SVG
            )
            SlugsEnum.NT.slug -> ImageLoader.load(
                    ClassLoader.getSystemResourceAsStream("assets/Cross.svg"),
                    ImageLoader.Format.SVG
            )
            else -> MaterialIconView(MaterialIcon.COLLECTIONS_BOOKMARK, "50px")
        }
    }

    private fun doesProjectExist(projectList: List<ProjectCollection>, thisCollection: Collection): Boolean {
        return projectList.map { it.titleKey }.contains(thisCollection.titleKey)
    }
}