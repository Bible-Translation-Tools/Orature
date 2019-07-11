package org.wycliffeassociates.otter.jvm.testapp.app

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.view.CardGridFragment
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view.MainScreenStyles
import org.wycliffeassociates.otter.jvm.app.ui.menu.view.MainMenu
import org.wycliffeassociates.otter.jvm.app.ui.resources.viewmodel.ResourceListViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view.RecordScriptureFragment
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*
import java.io.File

class RecordableVMTestView : View() {

    private val injector: Injector by inject()
    private val resourcesViewModel: ResourceListViewModel by inject()
    private val workbookViewModel: WorkbookViewModel by inject()

    private val directoryProvider = injector.directoryProvider
    private val collectionRepository = injector.collectionRepo
    val workbookRepository = injector.workbookRepository

    var activeFragment: Workspace = Workspace()

    override val root = vbox {}

    init {
        importStylesheet<MainScreenStyles>()

        activeFragment.root.apply {
            vgrow = Priority.ALWAYS
            addClass(MainScreenStyles.main)
        }

        activeFragment.add(MainMenu())

//        activeFragment.dock<CardGridFragment>()
        activeFragment.dock<RecordScriptureFragment>()

        add(activeFragment)

        setupResourcesViewModel()
    }

    private fun setupResourcesViewModel() {
        val targetProject = collectionRepository.getRootProjects().blockingGet().first()
        val sourceProject = collectionRepository.getSource(targetProject).blockingGet()
        val workbook = workbookRepository.get(sourceProject, targetProject)
        val chapter = workbook.target.chapters.blockingFirst()
        val chunk = chapter.chunks.blockingFirst()

//        workbookViewModel.activeResourceSlugProperty.set("tn")
        workbookViewModel.activeResourceSlugProperty.set("ulb")
        workbookViewModel.activeProjectAudioDirectoryProperty.set(getTestProjectAudioDirectory(workbook))

        workbookViewModel.activeWorkbookProperty.set(workbook)
        workbookViewModel.activeChapterProperty.set(chapter)
        workbookViewModel.activeChunkProperty.set(chunk)

        resourcesViewModel.loadResourceGroups()
    }

    private fun getTestProjectAudioDirectory(workbook: Workbook): File {
        val path = directoryProvider.getUserDataDirectory(
            "testAudioPath\\" +
                    "${workbook.target.language.slug}\\" +
                    "${workbook.target.slug}\\"
        )
        path.mkdirs()
        return path
    }
}