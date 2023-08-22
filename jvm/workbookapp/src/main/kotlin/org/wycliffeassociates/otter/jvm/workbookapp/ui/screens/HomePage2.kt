package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jfoenix.controls.JFXSnackbar
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportType
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.card.TranslationCard2
import org.wycliffeassociates.otter.jvm.controls.card.newTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.translationCreationCard
import org.wycliffeassociates.otter.jvm.controls.dialog.LoadingModal
import org.wycliffeassociates.otter.jvm.controls.dialog.ContributorDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.LanguageSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.controls.event.ProjectGroupDeleteEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportFinishEvent
import org.wycliffeassociates.otter.jvm.controls.model.NotificationStatusType
import org.wycliffeassociates.otter.jvm.controls.model.NotificationViewData
import org.wycliffeassociates.otter.jvm.controls.popup.NotificationSnackBar
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.ProjectImportEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookDeleteEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.ExportProjectDialog
import org.wycliffeassociates.otter.jvm.controls.dialog.ProgressDialog
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.controls.event.ProjectContributorsEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.BookSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.ProjectWizardSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportDialogOpenEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookOpenEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookQuickBackupEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ExportProjectViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*
import java.lang.Exception
import java.text.MessageFormat

class HomePage2 : View() {

    private val logger = LoggerFactory.getLogger(HomePage::class.java)
    private val listeners = mutableListOf<ListenerDisposer>()

    private val viewModel: HomePageViewModel2 by inject()
    private val projectWizardViewModel: ProjectWizardViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()
    private val exportProjectViewModel: ExportProjectViewModel by inject()
    private val navigator: NavigationMediator by inject()

    private lateinit var loadingModal: LoadingModal
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
        tryImportStylesheet("/css/contributor-info.css")
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
                        wizardFragment.onSectionDocked()
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

            visibleWhen {
                // hide left pane when window size is reduced
                primaryStage.widthProperty().greaterThan(1000.0) ?: booleanProperty(true)
            }
            managedWhen(visibleProperty())
        }
        center = stackpane {
            bindSingleChild(mainSectionProperty)
        }
    }

    override fun onDock() {
        super.onDock()
        setUpLoadingModal()
        viewModel.dock()
        navigator.dock(this, breadCrumb)
        mainSectionProperty.set(bookFragment)
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undock()
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
    }

    private fun subscribeActionEvents() {
        subscribe<LanguageSelectedEvent> {
            if (projectWizardViewModel.selectedSourceLanguageProperty.value == null) {
                wizardFragment.nextStep()
            }
            projectWizardViewModel.onLanguageSelected(it.item) {
                viewModel.loadProjects()
                mainSectionProperty.set(bookFragment)
            }
        }

        subscribe<ProjectContributorsEvent> {
            val books = it.books
            val dialog = find<ContributorDialog>().apply {
                themeProperty.set(settingsViewModel.appColorMode.value)
                orientationProperty.set(settingsViewModel.orientationProperty.value)
                contributors.setAll(viewModel.loadContributors(books))
                saveContributorCallback.set(
                    EventHandler {
                        viewModel.saveContributors(contributors, books)
                    }
                )
            }
            dialog.open()
        }

        subscribe<ProjectGroupDeleteEvent> {
            viewModel.deleteProjectGroup(it.books)
        }

        subscribe<WorkbookOpenEvent> {
            viewModel.selectBook(it.data)
        }

        subscribe<WorkbookDeleteEvent> {
            viewModel.deleteBook(it.data)
                .subscribe {
                    viewModel.loadProjects {
                        val notification = createBookDeleteNotification(it.data)
                        showNotification(notification)
                    }
                }
        }

        subscribe<WorkbookQuickBackupEvent> {
            val directory = chooseDirectory(FX.messages["exportProject"], owner = currentWindow)
            directory?.let { dir ->
                FX.eventbus.fire(
                    WorkbookExportEvent(
                        it.data,
                        ExportType.BACKUP,
                        dir,
                        chapters = null
                    )
                )
            }
        }

        subscribe<WorkbookExportDialogOpenEvent> {
            find<ExportProjectDialog> {
                val workbookDescriptor = it.data
                orientationProperty.set(settingsViewModel.orientationProperty.value)
                themeProperty.set(settingsViewModel.appColorMode.value)
                workbookDescriptorProperty.set(workbookDescriptor)

                exportProjectViewModel.loadChapters(workbookDescriptor)
                    .observeOnFx()
                    .subscribe { chapters ->
                        this.chapters.setAll(chapters)
                        open()
                    }

                setOnCloseAction { this.close() }
            }
        }

        subscribe<WorkbookExportEvent> { event ->
            handleExportEvent(event)
        }

        subscribe<WorkbookExportFinishEvent> {
            val notification = createExportNotification(it)
            showNotification(notification)
        }

        subscribe<ProjectImportEvent> { event ->
            logger.info("Import project event received, reloading projects...")
            if (event.result == ImportResult.SUCCESS) {
                viewModel.loadProjects {
                    val notification = createImportNotification(event)
                    showNotification(notification)
                }
            } else {
                val notification = createImportNotification(event)
                showNotification(notification)
            }

            event.workbookDescriptor?.let {
                viewModel.mergeContributorFromImport(it)
            }
        }
    }

    private fun setUpLoadingModal() {
        loadingModal = LoadingModal().apply {
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            themeProperty.set(settingsViewModel.appColorMode.value)
        }
        viewModel.isLoadingProperty.onChangeWithDisposer {
            if (it == true) {
                loadingModal.open()
            } else {
                loadingModal.close()
            }
        }.also { listeners.add(it) }
    }

    private fun exitWizard() {
        projectWizardViewModel.undock()
        viewModel.selectedProjectGroup.set(viewModel.projectGroups.firstOrNull()?.getKey())
        mainSectionProperty.set(bookFragment)
    }

    private fun handleExportEvent(event: WorkbookExportEvent) {
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
            cancelMessageProperty.set(messages["cancelExport"])

            setOnCloseAction {
                cancelMessageProperty.set(null)
                close()
            }
        }.apply { open() }

        exportProjectViewModel.exportWorkbook(
            event.workbook,
            event.outputDir,
            event.exportType,
            event.chapters
        )
            .observeOnFx()
            .doFinally {
                dialog.percentageProperty.set(0.0)
                dialog.cancelMessageProperty.set(null)
                dialog.close()
            }
            .subscribe { progressStatus ->
                progressStatus.percent?.let { percent ->
                    dialog.percentageProperty.set(percent)
                }
                progressStatus.titleKey?.let {
                    dialog.progressMessageProperty.set(messages[it])
                }
            }
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
                actionText = event.workbookDescriptor?.let { messages["openBook"] },
                actionIcon = event.workbookDescriptor?.let { MaterialDesign.MDI_ARROW_RIGHT }
            ) {
                /* open workbook callback */
                event.workbookDescriptor?.let { it ->
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
                        viewModel.openInFilesManager(filePath.path)
                    } catch (e: Exception) {
                        logger.error("Error while opening $filePath in file manager.")
                    }
                }
            }
        } else {
            NotificationViewData(
                title = messages["exportFailed"],
                message = MessageFormat.format(
                    messages["exportFailedMessage"],
                    event.project.titleKey,
                    event.project.resourceContainer?.language?.name ?: ""
                ),
                statusType = NotificationStatusType.FAILED
            )
        }
        return notification
    }

    private fun createBookDeleteNotification(workbookDescriptor: WorkbookDescriptor): NotificationViewData {
        return NotificationViewData(
            title = messages["projectDeleted"],
            message = MessageFormat.format(
                messages["projectDeletedMessage"],
                workbookDescriptor.targetCollection.titleKey,
                workbookDescriptor.targetLanguage.name
            ),
            statusType = NotificationStatusType.WARNING,
        )
    }

    private fun showNotification(notification: NotificationViewData) {
        val snackBar = JFXSnackbar(root)
        val graphic = NotificationSnackBar(notification).apply {
            setOnDismiss {
                snackBar.hide() /* avoid crashing if close() invoked before timeout */
            }
            setOnMainAction {
                notification.actionCallback()
            }
        }

        snackBar.enqueue(
            JFXSnackbar.SnackbarEvent(
                graphic,
                Duration.seconds(5.0)
            )
        )
    }
}
