package org.wycliffeassociates.otter.jvm.app.ui.mainscreen.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view.MainScreenView
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.view.CardGrid
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view.TakeManagementView
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class MainViewViewModel : ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()

    val selectedProjectName = SimpleStringProperty()
    val selectedProjectLanguage = SimpleStringProperty()

    val selectedChapterTitle = SimpleStringProperty()
    val selectedChapterBody = SimpleStringProperty()

    val selectedContentProperty = SimpleObjectProperty<Content>()
    val selectedContentTitle = SimpleStringProperty()
    val selectedContentBody = SimpleStringProperty()

    val takesPageDocked = SimpleBooleanProperty(false)

    init {
        workbookViewModel.activeWorkbookProperty.onChange {
            it?.let { wb -> projectSelected(wb) }
        }

        workbookViewModel.activeChapterProperty.onChange {
            it?.let { ch -> chapterSelected(ch) }
        }

        selectedContentProperty.onChange {
            if (it != null) {
                contentSelected(it)
            }
            else { // the take manager was undocked
                takesPageDocked.set(false)
            }
        }
    }

    private fun projectSelected(selectedWorkbook: Workbook) {
        setActiveProjectText(selectedWorkbook)

        find<MainScreenView>().activeFragment.dock<CardGrid>()
        CardGrid().apply {
            activeContent.bindBidirectional(selectedContentProperty)
        }
    }

    private fun chapterSelected(chapter: Chapter) {
        setActiveChapterText(chapter)
    }

    private fun contentSelected(content: Content) {
        setActiveContentText(content)

        if(takesPageDocked.value == false) {
            find<MainScreenView>().activeFragment.dock<TakeManagementView>()
            TakeManagementView().apply {
                activeContent.bindBidirectional(selectedContentProperty)
            }
        }
        takesPageDocked.set(true)
    }

    private fun setActiveContentText(content: Content) {
        selectedContentTitle.set(content.labelKey.toUpperCase())
        selectedContentBody.set(content.start.toString())
    }

    private fun setActiveChapterText(chapter: Chapter) {
        selectedChapterTitle.set(ContentLabel.CHAPTER.value.toUpperCase())
        selectedChapterBody.set(chapter.title)
    }

    private fun setActiveProjectText(activeWorkbook: Workbook) {
        selectedProjectName.set(activeWorkbook.target.title)
        selectedProjectLanguage.set(activeWorkbook.target.language.name)
    }
}