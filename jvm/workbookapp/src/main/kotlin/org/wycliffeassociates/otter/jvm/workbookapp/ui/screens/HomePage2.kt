package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jfoenix.controls.JFXSnackbar
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.card.TranslationCard2
import org.wycliffeassociates.otter.jvm.controls.card.newTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.translationCreationCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.LanguageSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportDialogOpenEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportFinishEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookOpenEvent
import org.wycliffeassociates.otter.jvm.controls.model.NotificationStatusType
import org.wycliffeassociates.otter.jvm.controls.model.NotificationViewData
import org.wycliffeassociates.otter.jvm.controls.popup.NotificationSnackBar
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.ProjectImportEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.ExportProjectDialog
import org.wycliffeassociates.otter.jvm.controls.dialog.ProgressDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.BookSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.ProjectWizardSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.openInFilesManager
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ExportProjectViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*
import java.lang.Exception
import java.text.MessageFormat


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
    private lateinit var snackBar: JFXSnackbar

    init {
        tryImportStylesheet("/css/control.css")
        tryImportStylesheet("/css/home-page.css")
        tryImportStylesheet("/css/translation-card-2.css")
        tryImportStylesheet("/css/popup-menu.css")
        tryImportStylesheet("/css/filtered-search-bar.css")
        tryImportStylesheet("/css/table-view.css")
        tryImportStylesheet("/css/confirm-dialog.css")
        tryImportStylesheet("/css/import-export-dialogs.css")
        tryImportStylesheet("/css/progress-dialog.css")
        tryImportStylesheet("/css/card-radio-btn.css")
        tryImportStylesheet("/css/snack-bar-notification.css")

        subscribeActionEvents()
    }

    override val root = borderpane {
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

                setOnCloseAction { this.close() }
            }.open()
        }

        subscribe<WorkbookExportEvent> { event ->
            val dialog = find<ProgressDialog> {
                orientationProperty.set(settingsViewModel.orientationProperty.value)
                themeProperty.set(settingsViewModel.appColorMode.value)
                dialogTitleProperty.set(
                    MessageFormat.format(
                        messages["exportProjectTitle"],
                        messages["exporting"],
                        event.workbook.title
                    )
                )
                dialogMessageProperty.set(messages["exportProjectMessage"])

                setOnCloseAction { close() }
            }.apply { open() }

            exportProjectViewModel.exportWorkbook(
                event.workbook,
                event.outputDir,
                event.exportType,
                event.chapters
            )
                .observeOnFx()
                .doOnComplete {
                    dialog.percentageProperty.unbind()
                    dialog.close()
                }
                .subscribe { progressStatus ->
                    progressStatus.percent?.let { percent ->
                        dialog.percentageProperty.set(percent)
                    }
                    dialog.progressMessageProperty.set(progressStatus.titleKey)
                }
        }

        subscribe<WorkbookExportFinishEvent> {
            val notification = createExportNotification(it)
            showNotification(notification)
        }

        subscribe<ProjectImportEvent> {
            logger.info("Import project event received, refreshing the homepage.")
            val notification = createImportNotification(it)
            showNotification(notification)
            viewModel.refresh()
        }
    }

    private fun exitWizard() {
        projectWizardViewModel.undock()
        viewModel.loadProjects()
        mainSectionProperty.set(bookFragment)
    }

    private fun createImportNotification(event: ProjectImportEvent): NotificationViewData {
        if (event.result == ImportResult.FAILED) {
            return NotificationViewData(
                title = messages["importFailed"],
                message = MessageFormat.format(messages["importFailedMessage"], event.filePath),
                statusType = NotificationStatusType.FAILED
            )
        }

        return if (event.project != null) {
            // single project import
            val messageBody = MessageFormat.format(
                messages["importProjectSuccessfulMessage"],
                event.project,
                event.language
            )
            NotificationViewData(
                title = messages["importSuccessful"],
                message = messageBody,
                statusType = NotificationStatusType.SUCCESSFUL,
                actionText = messages["openBook"],
                actionIcon = MaterialDesign.MDI_ARROW_RIGHT
            ) {
                /* open workbook callback */
                event.workbook?.let { it ->
                    viewModel.selectBook(it)
                }
            }
        } else {
            // source import
            val messageBody = MessageFormat.format(
                messages["importSourceSuccessfulMessage"],
                event.language
            )
            NotificationViewData(
                title = messages["importSuccessful"],
                message = messageBody,
                statusType = NotificationStatusType.SUCCESSFUL,
            )
        }
    }

    private fun createExportNotification(event: WorkbookExportFinishEvent): NotificationViewData {
        val notification = if (event.result == ExportResult.SUCCESS) {
            val messageBody = MessageFormat.format(
                messages["exportSuccessfulMessage"],
                event.project.titleKey,
                event.project.resourceContainer?.language?.name
            )
            NotificationViewData(
                title = messages["exportSuccessful"],
                message = messageBody,
                statusType = NotificationStatusType.SUCCESSFUL,
                actionText = messages["showLocation"],
                actionIcon = MaterialDesign.MDI_OPEN_IN_NEW
            ) {
                val filePath = event.file
                if (filePath?.exists() == true) {
                    try {
                        openInFilesManager(filePath.path)
                    } catch (e: Exception) {
                        logger.error("Error while opening $filePath in file manager.")
                    }
                }
            }
        } else {
            NotificationViewData(
                title = messages["exportFailed"],
                message = MessageFormat.format(messages["exportFailedMessage"], event.project.titleKey),
                statusType = NotificationStatusType.FAILED
            )
        }
        return notification
    }

    private fun showNotification(notification: NotificationViewData) {
        val snackBar = JFXSnackbar(root)
        val graphic = NotificationSnackBar().apply {

            titleProperty.set(notification.title)
            messageProperty.set(notification.message)
            statusTypeProperty.set(notification.statusType)
            actionIconProperty.set(notification.actionIcon)
            actionTextProperty.set(notification.actionText)

            setOnDismiss {
                snackBar.hide() /* avoid crashing if dismiss before timeout */
            }
            setOnMainAction {
                notification.actionCallback()
            }
        }

        snackBar.enqueue(
            JFXSnackbar.SnackbarEvent(
                graphic.build(),
                Duration.seconds(5.0)
            )
        )
    }
}
