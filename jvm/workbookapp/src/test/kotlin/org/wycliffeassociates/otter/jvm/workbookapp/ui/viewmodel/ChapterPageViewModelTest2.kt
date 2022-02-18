package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import javafx.beans.property.SimpleObjectProperty
import org.junit.Assert
import org.junit.Test
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import tornadofx.*

class ChapterPageViewModelTest2 {

    @Test
    fun `select chapter card on card selection`() {
        val testApp: TestApp = TestApp()
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication { testApp }

        val chapter = mock<Chapter> {
            on { sort } doReturn 5
        }
        val chapterCard = mock<CardData>() {
            on { chapterSource } doReturn chapter
        }

        val chapProp = SimpleObjectProperty<Chapter>()
        val chunkProp = SimpleObjectProperty<Chunk>()
        val workbookDataStore = mock<WorkbookDataStore> {
            on { activeChapterProperty } doReturn chapProp
            on { activeChunkProperty } doReturn chunkProp
        }

        FX.getComponents()[WorkbookDataStore::class] = workbookDataStore

        val chapterPageViewModel: ChapterPageViewModel = find()
        chapterPageViewModel.onCardSelection(chapterCard)
        Assert.assertNotNull(workbookDataStore.activeChapterProperty.value)

        FxToolkit.hideStage()
        FxToolkit.cleanupStages()
        FxToolkit.cleanupApplication(testApp)
    }
}