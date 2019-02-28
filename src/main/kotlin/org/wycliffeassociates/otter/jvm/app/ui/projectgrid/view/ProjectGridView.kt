package org.wycliffeassociates.otter.jvm.app.ui.projectgrid.view

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.*
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.images.ImageLoader
import org.wycliffeassociates.otter.jvm.app.images.SVGImage
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.ui.projectgrid.viewmodel.ProjectGridViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.app.widgets.card.card
import tornadofx.*

class ProjectGridView : Fragment() {

    private val viewModel: ProjectGridViewModel by inject()
    private val noProjectsProperty: ReadOnlyBooleanProperty

    val activeProject: Property<Collection> = viewModel.selectedProjectProperty

    init {
        importStylesheet<ProjectGridStyles>()
        importStylesheet<DefaultStyles>()
        // Setup property bindings to bind to empty property
        // https://stackoverflow.com/questions/21612969/is-it-possible-to-bind-the-non-empty-state-of-
        // an-observablelist-inside-an-object
        val listProperty = SimpleListProperty<Collection>()
        listProperty.bind(SimpleObjectProperty(viewModel.projects))
        noProjectsProperty = listProperty.emptyProperty()
    }

    override val root = anchorpane {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS
        addClass(AppStyles.appBackground)

        datagrid(viewModel.projects) {
            anchorpaneConstraints {
                topAnchor = 0
                rightAnchor = 0
                bottomAnchor = 0
                leftAnchor = 0
            }
            addClass(AppStyles.appBackground)
            addClass(ProjectGridStyles.projectsGrid)
            cellCache {
                val item = it
                card {
                    addClass(DefaultStyles.defaultCard)
                    cardfront {
                        isActive = true
                        innercard(AppStyles.projectGraphic()) {
                            majorLabel = it.titleKey
                            minorLabel = it.resourceContainer?.language?.name
                        }
                        cardbutton {
                            addClass(DefaultStyles.defaultCardButton)
                            text = messages["openProject"]
                            graphic = MaterialIconView(MaterialIcon.ARROW_FORWARD, "25px")
                                    .apply { fill = AppTheme.colors.appRed }
                            onMousePressed = EventHandler {
                                viewModel.selectProject(item)
                            }
                        }

                    }
                }
            }
        }

        vbox {
            anchorpaneConstraints {
                topAnchor = 0
                leftAnchor = 0
                bottomAnchor = 0
                rightAnchor = 0
            }
            alignment = Pos.CENTER
            vgrow = Priority.ALWAYS
            label(messages["noProjects"]) {
                addClass(ProjectGridStyles.noProjectsLabel)
            }
            label(messages["noProjectsSubtitle"]) {
                addClass(ProjectGridStyles.tryCreatingLabel)
            }

            visibleProperty().bind(noProjectsProperty)
            managedProperty().bind(visibleProperty())
        }

        add(JFXButton("", MaterialIconView(MaterialIcon.ADD, "25px")).apply {
            addClass(ProjectGridStyles.addProjectButton)
            isDisableVisualFocus = true
            anchorpaneConstraints {
                bottomAnchor = 25
                rightAnchor = 25
            }
            action { viewModel.createProject() }
        })
    }

    init {
        with(root) {
            add(ImageLoader.load(
                    ClassLoader.getSystemResourceAsStream("images/project_home_arrow.svg"),
                    ImageLoader.Format.SVG
            ).apply {
                if (this is SVGImage) preserveAspect = false
                root.widthProperty().onChange {
                    anchorpaneConstraints { leftAnchor = it / 2.0 }
                }
                root.heightProperty().onChange {
                    anchorpaneConstraints { topAnchor = it / 2.0 + 75.0 }
                }
                anchorpaneConstraints {
                    rightAnchor = 125
                    bottomAnchor = 60
                }

                visibleProperty().bind(noProjectsProperty)
                managedProperty().bind(visibleProperty())
            })
        }
    }

    override fun onDock() {
        viewModel.loadProjects()
        viewModel.clearSelectedProject()
    }
}