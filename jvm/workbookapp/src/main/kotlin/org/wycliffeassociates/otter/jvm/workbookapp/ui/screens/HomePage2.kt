package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.jfoenix.controls.JFXSnackbar
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.card.TranslationCard2
import org.wycliffeassociates.otter.jvm.controls.card.newTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.translationCreationCard
import org.wycliffeassociates.otter.jvm.controls.event.LanguageSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookExportDialogOpenEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookExportFinishEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookOpenEvent
import org.wycliffeassociates.otter.jvm.controls.model.NotificationStatusType
import org.wycliffeassociates.otter.jvm.controls.model.NotificationViewData
import org.wycliffeassociates.otter.jvm.controls.popup.NotificationSnackBar
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.ExportProjectDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.ImportEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.ProjectWizardSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.BookSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ExportProjectViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class HomePage2 : View() {

    private val logger = LoggerFactory.getLogger(HomePage::class.java)

    private val viewModel: HomePageViewModel2 by inject()
    private val projectWizardViewModel: ProjectWizardViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()
    private val exportProjectViewModel: ExportProjectViewModel by inject()
    private val navigator: NavigationMediator by inject()

    private val mainSectionProperty = SimpleObjectProperty<Node>(null)
    private val breadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["home"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_HOME))
        setOnAction {
            fire(NavigationRequestEvent(this@HomePage2))
        }
    }
    private val bookFragment = BookSection(viewModel.bookList, viewModel.sortedBooks).apply {
        bookSearchQueryProperty.bindBidirectional(viewModel.bookSearchQueryProperty)
    }
    private val wizardFragment: ProjectWizardSection by lazy {
        ProjectWizardSection(
            projectWizardViewModel.sortedSourceLanguages,
            projectWizardViewModel.sortedTargetLanguages,
            projectWizardViewModel.selectedModeProperty,
            projectWizardViewModel.selectedSourceLanguageProperty
        ).apply {

            sourceLanguageSearchQueryProperty.bindBidirectional(projectWizardViewModel.sourceLanguageSearchQueryProperty)
            targetLanguageSearchQueryProperty.bindBidirectional(projectWizardViewModel.targetLanguageSearchQueryProperty)

            setOnCancelAction {
                exitWizard()
            }
        }
    }

    init {
        tryImportStylesheet("/css/control.css")
        tryImportStylesheet("/css/home-page.css")
        tryImportStylesheet("/css/translation-card-2.css")
        tryImportStylesheet("/css/popup-menu.css")
        tryImportStylesheet("/css/filtered-search-bar.css")
        tryImportStylesheet("/css/table-view.css")
        tryImportStylesheet("/css/confirm-dialog.css")
        tryImportStylesheet("/css/import-export-dialogs.css")
        tryImportStylesheet("/css/card-radio-btn.css")
        tryImportStylesheet("/css/snack-bar-notification.css")

        subscribeActionEvents()
    }

    private fun subscribeActionEvents() {
        subscribe<LanguageSelectedEvent> {
            projectWizardViewModel.onLanguageSelected(it.item) {
                viewModel.loadProjects()
                mainSectionProperty.set(bookFragment)
            }
        }

        subscribe<WorkbookOpenEvent> {
            viewModel.selectBook(it.data)
        }

        subscribe<WorkbookExportDialogOpenEvent> {
            find<ExportProjectDialog> {
                val workbookDescriptor = it.data
                orientationProperty.set(settingsViewModel.orientationProperty.value)
                themeProperty.set(settingsViewModel.appColorMode.value)
                workbookDescriptorProperty.set(workbookDescriptor)

                exportProjectViewModel.loadAvailableChapters(workbookDescriptor)
                    .subscribe { chapters ->
                        availableChapters.setAll(chapters)
                    }

                setOnCloseAction {
                    this.close()
                }
            }.open()
        }

        subscribe<WorkbookExportEvent> { event ->
            exportProjectViewModel.exportWorkbook(
                event.workbook,
                event.outputDir,
                event.exportType,
                event.chapters
            )
        }

        subscribe<WorkbookExportFinishEvent> {
            val notification = NotificationViewData(
                titleKey = "exportSuccessful",
                subtitleKey = "exportSuccessfulPopupMessage",
                statusType = NotificationStatusType.SUCCESSFUL
            )
            fireNotification(notification)
        }

        subscribe<ImportEvent> {
            logger.info("Import project event received, refreshing the homepage.")
            val notification = NotificationViewData(
                titleKey = "importSuccessful",
                subtitleKey = "importSuccessfulPopupMessage",
                statusType = NotificationStatusType.SUCCESSFUL
            ) {
                println("opening book")
            }
            fireNotification(notification)
            viewModel.refresh()
        }
    }

    override val root = borderpane {
        createSnackBar(this)
        left = vbox {
            addClass("homepage__left-pane")
            label(messages["projects"]) {
                addClass("h3", "h3--80", "homepage__left-header")
            }
            stackpane {
                translationCreationCard {
                    visibleWhen { mainSectionProperty.isNotEqualTo(wizardFragment) }
                    managedWhen(visibleProperty())
                    setOnAction {
                        viewModel.selectedProjectGroup.set(null)
                        mainSectionProperty.set(wizardFragment)
                        projectWizardViewModel.dock()
                    }
                }
                newTranslationCard(
                    projectWizardViewModel.selectedSourceLanguageProperty,
                    projectWizardViewModel.selectedTargetLanguageProperty,
                    projectWizardViewModel.selectedModeProperty
                ) {
                    visibleWhen { mainSectionProperty.isEqualTo(wizardFragment) }
                    managedWhen(visibleProperty())

                    setOnCancelAction {
                        exitWizard()
                    }
                }
            }

            button("show toast") {
                action {
                    val notification = NotificationViewData(
                        titleKey = "importSuccessful",
                        subtitleKey = "importSuccessfulPopupMessage",
                        statusType = NotificationStatusType.SUCCESSFUL
                    ) {
                        println("opening book")
                    }
                    viewModel.snackBarObservable.onNext(
                        notification
                    )
                }
            }

            scrollpane {
                vgrow = Priority.ALWAYS
                isFitToWidth = true

                vbox { /* list of project groups */
                    addClass("homepage__left-pane__project-groups")
                    bindChildren(viewModel.projectGroups) { cardModel ->
                        TranslationCard2(
                            cardModel.sourceLanguage,
                            cardModel.targetLanguage,
                            cardModel.mode,
                            viewModel.selectedProjectGroup
                        ).apply {

                            setOnAction {
                                viewModel.bookList.setAll(cardModel.books)
                                viewModel.selectedProjectGroup.set(cardModel.getKey())
                                if (mainSectionProperty.value !is BookSection) {
                                    exitWizard()
                                }
                            }
                        }
                    }
                }
            }
        }
        center = stackpane {
            bindSingleChild(mainSectionProperty)
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.dock()
        navigator.dock(this, breadCrumb)
        mainSectionProperty.set(bookFragment)
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undock()
    }

    private fun exitWizard() {
        projectWizardViewModel.undock()
        viewModel.loadProjects()
        mainSectionProperty.set(bookFragment)
    }

    private fun createSnackBar(pane: Pane) {
        val snackBar = JFXSnackbar(pane)
        viewModel.snackBarObservable.subscribe { notificationData ->

            val graphic = NotificationSnackBar().apply {

                titleProperty.set(messages[notificationData.titleKey])
                subtitleProperty.set(messages[notificationData.subtitleKey])
                statusTypeProperty.set(notificationData.statusType)

                if (notificationData.mainAction != null) {
                    setMainActionGraphic(
                        button(messages["open"]) {
                            addClass("btn", "btn--secondary")
                            graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                            action {
                                notificationData.mainAction!!()
                            }
                        }
                    )
                } else {
                    setMainActionGraphic(Region())
                }


                setOnDismiss { snackBar.close() }
//                setOnMainAction { FX.eventbus.fire(WorkbookOpenEvent()) }

            }
            snackBar.enqueue(
                JFXSnackbar.SnackbarEvent(
                    graphic.build(),
                    Duration.seconds(5.0)
                )
            )
        }
    }

    private fun fireNotification(notification: NotificationViewData) {
        viewModel.snackBarObservable.onNext(
            notification
        )
    }
}
