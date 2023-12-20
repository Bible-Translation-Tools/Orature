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
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.dialog.ImportProjectDialog
import org.wycliffeassociates.otter.jvm.controls.event.LanguageSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.controls.event.ProjectGroupDeleteEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportFinishEvent
import org.wycliffeassociates.otter.jvm.controls.model.NotificationStatusType
import org.wycliffeassociates.otter.jvm.controls.model.NotificationViewData
import org.wycliffeassociates.otter.jvm.controls.popup.NotificationSnackBar
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.controls.event.ProjectImportFinishEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookDeleteEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.ExportProjectDialog
import org.wycliffeassociates.otter.jvm.controls.dialog.ProgressDialog
import org.wycliffeassociates.otter.jvm.controls.event.ProjectImportEvent
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.controls.event.ProjectContributorsEvent
import org.wycliffeassociates.otter.jvm.controls.model.ProjectGroupCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.BookSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home.ProjectWizardSection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportDialogOpenEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookOpenEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookQuickBackupEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ImportProjectViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ExportProjectViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.NOTIFICATION_DURATION_SEC
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*
import java.io.File
import java.lang.Exception
import java.text.MessageFormat

class HomePage2 : View() {

    private val logger = LoggerFactory.getLogger(HomePage::class.java)
    private val listeners = mutableListOf<ListenerDisposer>()

    private val viewModel: HomePageViewModel2 by inject()
    private val importProjectViewModel: ImportProjectViewModel by inject()

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
            projectWizardViewModel.selectedSourceLanguageProperty,
            projectWizardViewModel.existingLanguagePairs
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
        tryImportStylesheet("/css/app-drawer.css")
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
                        viewModel.selectedProjectGroupProperty.set(null)
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
                            viewModel.selectedProjectGroupProperty
                        ).apply {

                            setOnAction {
                                viewModel.bookList.setAll(cardModel.books)
                                viewModel.selectedProjectGroupProperty.set(cardModel.getKey())
                                if (mainSectionProperty.value !is BookSection) {
                                    exitWizard()
                                }
                            }
                        }
                    }
                }

                runLater { customizeScrollbarSkin() }
            }

            button(messages["import"]) {
                addClass("btn", "btn--secondary")
                tooltip(text)
                graphic = FontIcon(MaterialDesign.MDI_DOWNLOAD)
                useMaxWidth = true

                action {
                    showImportModal()
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
            } else {
                // open loading dialog when creating project
                viewModel.isLoadingProperty.set(true)
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
                viewModel.loadContributors(books.first())
                    .subscribe { list ->
                        contributors.setAll(list)
                    }

                saveContributorCallback.set(
                    EventHandler {
                        viewModel.saveContributors(contributors, books.first())
                    }
                )
            }
            dialog.open()
        }

        subscribe<ProjectGroupDeleteEvent> {
            val cardModel = viewModel.projectGroups
                .find { gr -> gr.getKey() == viewModel.selectedProjectGroupProperty.value }
                ?: return@subscribe

            viewModel.removeProjectFromList(cardModel)
            viewModel.deleteProjectGroupWithTimer(cardModel)
            val notification = createProjectGroupDeleteNotification(cardModel)
            showNotification(notification)
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

        subscribe<ProjectImportFinishEvent> { event ->
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

        subscribe<ProjectImportEvent> {
            handleImportFile(it.file)
        }
    }

    private fun setUpLoadingModal() {
        find<LoadingModal>().apply {
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            themeProperty.set(settingsViewModel.appColorMode.value)
            viewModel.isLoadingProperty.onChangeWithDisposer {
                if (it == true) {
                    open()
                } else {
                    close()
                }
            }.also { listeners.add(it) }
        }
    }

    private fun exitWizard() {
        projectWizardViewModel.undock()
        viewModel.selectedProjectGroupProperty.set(viewModel.projectGroups.firstOrNull()?.getKey())
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

    private fun handleImportFile(file: File) {
        importProjectViewModel.setProjectInfo(file)

        val dialog = setupImportProgressDialog()

        importProjectViewModel.importProject(file)
            .observeOnFx()
            .doFinally {
                dialog.dialogTitleProperty.unbind()
                dialog.percentageProperty.set(0.0)
                dialog.close()
            }
            .subscribe { progressStatus ->
                progressStatus.percent?.let { percent ->
                    dialog.percentageProperty.set(percent)
                }
                if (progressStatus.titleKey != null && progressStatus.titleMessage != null) {
                    val message = MessageFormat.format(messages[progressStatus.titleKey!!], messages[progressStatus.titleMessage!!])
                    dialog.progressMessageProperty.set(message)
                } else if (progressStatus.titleKey != null) {
                    dialog.progressMessageProperty.set(messages[progressStatus.titleKey!!])
                }
            }
    }

    private fun setupImportProgressDialog() = find<ProgressDialog> {
        orientationProperty.set(settingsViewModel.orientationProperty.value)
        themeProperty.set(settingsViewModel.appColorMode.value)
        allowCloseProperty.set(false)
        cancelMessageProperty.set(null)
        dialogTitleProperty.bind(importProjectViewModel.importedProjectTitleProperty.stringBinding {
            it?.let {
                MessageFormat.format(
                    messages["importProjectTitle"],
                    messages["import"],
                    it
                )
            } ?: messages["importResource"]
        })

        setOnCloseAction { close() }

        open()
    }

    private fun createImportNotification(event: ProjectImportFinishEvent): NotificationViewData {
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
            title = messages["bookDeleted"],
            message = MessageFormat.format(
                messages["bookDeletedMessage"],
                workbookDescriptor.targetCollection.titleKey,
                workbookDescriptor.targetLanguage.name
            ),
            statusType = NotificationStatusType.WARNING,
        )
    }

    private fun createProjectGroupDeleteNotification(
        cardModel: ProjectGroupCardModel
    ): NotificationViewData {
        return NotificationViewData(
            title = messages["projectDeleted"],
            message = MessageFormat.format(
                messages["projectDeletedMessage"],
                cardModel.sourceLanguage.name,
                cardModel.targetLanguage.name,
                messages[cardModel.mode.titleKey]
            ),
            statusType = NotificationStatusType.WARNING,
            actionText = messages["undo"],
            actionIcon = MaterialDesign.MDI_UNDO
        ) {
            viewModel.undoDeleteProjectGroup(cardModel)
        }
    }

    private fun showNotification(notification: NotificationViewData) {
        val snackBar = JFXSnackbar(root)
        val graphic = NotificationSnackBar(notification).apply {
            setOnDismiss {
                snackBar.hide() /* avoid crashing if close() invoked before timeout */
            }
            setOnMainAction {
                notification.actionCallback()
                snackBar.hide()
            }
        }

        snackBar.enqueue(
            JFXSnackbar.SnackbarEvent(
                graphic,
                Duration.seconds(NOTIFICATION_DURATION_SEC)
            )
        )
    }

    private fun showImportModal() {
        find<ImportProjectDialog>().apply {
            themeProperty.set(settingsViewModel.appColorMode.value)
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            validateFileCallback.set {
                importProjectViewModel.isValidImportFile(it)
            }
            open()
        }
    }
}
