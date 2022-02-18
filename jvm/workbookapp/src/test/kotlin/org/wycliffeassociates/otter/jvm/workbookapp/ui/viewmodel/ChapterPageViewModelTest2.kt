package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.jakewharton.rxrelay2.BehaviorRelay
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import javafx.beans.property.SimpleObjectProperty
import kotlin.properties.Delegates.notNull
import org.junit.Assert
import org.junit.Test
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.utils.writeWavFile
import tornadofx.*
import java.io.File
import java.time.LocalDate
import java.util.*

class ChapterPageViewModelTest2 {

    @Test
    fun `select chapter card on card selection`() {
        val testApp: TestApp = TestApp()
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication { testApp }

        val chunk = mock<Chunk> {
            on { sort } doReturn 3
        }
        val chapter = mock<Chapter> {
            on { sort } doReturn 5
        }
        val chapterCard = mock<CardData>() {
            on { chapterSource } doReturn chapter
        }

        val chapProp = SimpleObjectProperty<Chapter>()
        val chunkProp = SimpleObjectProperty<Chunk>(chunk)
        val workbookDataStore = mock<WorkbookDataStore> {
            on { activeChapterProperty } doReturn chapProp
            on { activeChunkProperty } doReturn chunkProp
        }

        FX.getComponents()[WorkbookDataStore::class] = workbookDataStore

        val chapterPageViewModel: ChapterPageViewModel = find()
        Assert.assertNull(workbookDataStore.activeChapterProperty.value)
        Assert.assertEquals(3, workbookDataStore.activeChunkProperty.value.sort)
        chapterPageViewModel.onCardSelection(chapterCard)
        Assert.assertEquals(5, workbookDataStore.activeChapterProperty.value.sort)
        Assert.assertNull(workbookDataStore.activeChunkProperty.value)

        val chapterCard2 = mock<CardData>()
        workbookDataStore.activeChapterProperty.set(null)
        workbookDataStore.activeChunkProperty.set(chunk)
        Assert.assertNull(workbookDataStore.activeChapterProperty.value)
        Assert.assertNotNull(workbookDataStore.activeChunkProperty.value)
        chapterPageViewModel.onCardSelection(chapterCard2)
        Assert.assertNull(workbookDataStore.activeChapterProperty.value)
        Assert.assertNull(workbookDataStore.activeChunkProperty.value)

        FxToolkit.hideStage()
        FxToolkit.cleanupStages()
        FxToolkit.cleanupApplication(testApp)
    }

    @Test
    fun `select chunk card on card selection`() {
        val testApp: TestApp = TestApp()
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication { testApp }

        val oldChunk = mock<Chunk> {
            on { sort } doReturn 1
        }
        val chunk = mock<Chunk> {
            on { sort } doReturn 3
        }
        val chapter = mock<Chapter> {
            on { sort } doReturn 5
        }
        val chapterCard = mock<CardData>() {
            on { chapterSource } doReturn chapter
            on { chunkSource } doReturn chunk
        }

        val chapProp = SimpleObjectProperty<Chapter>(chapter)
        val chunkProp = SimpleObjectProperty<Chunk>(oldChunk)
        val workbookDataStore = mock<WorkbookDataStore> {
            on { activeChapterProperty } doReturn chapProp
            on { activeChunkProperty } doReturn chunkProp
        }

        FX.getComponents()[WorkbookDataStore::class] = workbookDataStore

        val chapterPageViewModel: ChapterPageViewModel = find()
        Assert.assertEquals(5, workbookDataStore.activeChapterProperty.value.sort)
        Assert.assertEquals(1, workbookDataStore.activeChunkProperty.value.sort)
        chapterPageViewModel.onCardSelection(chapterCard)
        Assert.assertEquals(5, workbookDataStore.activeChapterProperty.value.sort)
        Assert.assertEquals(3, workbookDataStore.activeChunkProperty.value.sort)

        FxToolkit.hideStage()
        FxToolkit.cleanupStages()
        FxToolkit.cleanupApplication(testApp)
    }

    @Test
    fun `when not all chunks selected, canCompile is false`() {
        val testApp: TestApp = TestApp()
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication { testApp }

        val chapterPageViewModel: ChapterPageViewModel = find()

        Assert.assertFalse(chapterPageViewModel.canCompileProperty.value)

        val mockedTake = mock<Take>()
        val mockedTakeHolder = mock<TakeHolder> { on { value } doReturn mockedTake }
        val mockedRelay = mock<BehaviorRelay<TakeHolder>> { on { value } doReturn mockedTakeHolder }
        val mockedAudio = mock<AssociatedAudio> { on { selected } doReturn mockedRelay }
        val mockedChunk = mock<Chunk> { on { audio } doReturn mockedAudio }
        val cardData1 = mock<CardData> { on { chunkSource } doReturn mockedChunk }
        val cardData2 = mock<CardData>()

        val list = observableListOf<CardData>(cardData1, cardData2)

        chapterPageViewModel.filteredContent.setAll(list)

        chapterPageViewModel.checkCanCompile()
        Assert.assertFalse(chapterPageViewModel.canCompileProperty.value)

        FxToolkit.hideStage()
        FxToolkit.cleanupStages()
        FxToolkit.cleanupApplication(testApp)
    }

    @Test
    fun `when all chunks selected, canCompile is true`() {
        val testApp: TestApp = TestApp()
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication { testApp }

        val chapterPageViewModel: ChapterPageViewModel = find()

        Assert.assertFalse(chapterPageViewModel.canCompileProperty.value)

        val mockedTake = mock<Take>()
        val mockedTakeHolder = mock<TakeHolder> { on { value } doReturn mockedTake }
        val mockedRelay = mock<BehaviorRelay<TakeHolder>> { on { value } doReturn mockedTakeHolder }
        val mockedAudio = mock<AssociatedAudio> { on { selected } doReturn mockedRelay }
        val mockedChunk = mock<Chunk> { on { audio } doReturn mockedAudio }
        val cardData1 = mock<CardData> { on { chunkSource } doReturn mockedChunk }
        val cardData2 = mock<CardData> { on { chunkSource } doReturn mockedChunk }

        val list = observableListOf<CardData>(cardData1, cardData2)

        chapterPageViewModel.filteredContent.setAll(list)

        chapterPageViewModel.checkCanCompile()
        Assert.assertTrue(chapterPageViewModel.canCompileProperty.value)

        FxToolkit.hideStage()
        FxToolkit.cleanupStages()
        FxToolkit.cleanupApplication(testApp)
    }
}
