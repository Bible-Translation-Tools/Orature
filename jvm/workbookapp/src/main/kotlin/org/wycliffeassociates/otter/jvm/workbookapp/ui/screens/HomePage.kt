package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.banner.ResumeBookBanner
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.card.BookCard
import org.wycliffeassociates.otter.jvm.controls.card.NewTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.TranslationCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class HomePage : Fragment() {

    private val viewModel: HomePageViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            workbookDataStore.activeWorkbookProperty.stringBinding {
                it?.target?.title ?: messages["project"]
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_BOOK))
        onClickAction {
            navigator.dock(this@HomePage)
        }
    }

    init {
        importStylesheet(resources.get("/css/root.css"))
        importStylesheet(resources.get("/css/control.css"))
        importStylesheet(resources.get("/css/home-page.css"))
        importStylesheet(resources.get("/css/resume-book-banner.css"))
        importStylesheet(resources.get("/css/new-translation-card.css"))
        importStylesheet(resources.get("/css/translation-card.css"))
        importStylesheet(resources.get("/css/book-card.css"))
    }

    override val root = stackpane {
        alignment = Pos.TOP_LEFT

        scrollpane {
            vbox {
                addClass("home-page__container")

                add(
                    ResumeBookBanner().apply {
                        resumeTextProperty.set(messages["resume"])

                        viewModel.resumeBookProperty.onChange {
                            it?.let { workbook ->
                                bookTitleProperty.set(workbook.target.title)
                                backgroundImageFileProperty.set(workbook.coverArtAccessor.getArtwork())
                                sourceLanguageProperty.set(workbook.source.language.name)
                                targetLanguageProperty.set(workbook.target.language.name)
                                onResumeAction {
                                    viewModel.selectProject(workbook)
                                }
                            }
                        }

                        visibleProperty().bind(viewModel.resumeBookProperty.isNotNull)
                        managedProperty().bind(visibleProperty())
                    }
                )

                add(
                    NewTranslationCard().apply {
                        newTranslationTextProperty.set(messages["newTranslation"])
                        setOnAction {
                            viewModel.createTranslation()
                        }
                    }
                )

                vbox {
                    spacing = 20.0
                    bindChildren(viewModel.translationModels) {
                        TranslationCard(it.sourceLanguage.name, it.targetLanguage.name, it.books).apply {
                            setConverter {
                                BookCard().apply {
                                    titleProperty.set(it.target.title)
                                    coverArtProperty.set(it.coverArtAccessor.getArtwork())

                                    setOnPrimaryAction { viewModel.selectProject(it) }
                                }
                            }

                            seeMoreTextProperty.set(messages["seeMore"])
                            seeLessTextProperty.set(messages["seeLess"])

                            setOnNewBookAction {
                                viewModel.createProject(it)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        navigator.dock(this, breadCrumb)
        viewModel.loadResumeBook()
        viewModel.loadTranslations()
        viewModel.clearSelectedProject()
        workbookDataStore.activeWorkbookProperty.set(null)
    }
}
