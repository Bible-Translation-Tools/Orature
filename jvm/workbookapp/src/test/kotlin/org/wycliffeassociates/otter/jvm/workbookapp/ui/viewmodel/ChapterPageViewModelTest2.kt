package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
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
import org.junit.After
import org.junit.Before

class ChapterPageViewModelTest2 {

    private lateinit var testApp: TestApp

    @Before
    fun setup() {
        testApp = TestApp()
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication { testApp }
    }

    @After
    fun teardown() {
        FxToolkit.hideStage()
        FxToolkit.cleanupStages()
        FxToolkit.cleanupApplication(testApp)
    }

    @Test
    fun `select chapter card on card selection`() {
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
    }

    @Test
    fun `select chunk card on card selection`() {
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
    }

    @Test
    fun `when not all chunks selected, canCompile is false`() {
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
    }

    @Test
    fun `when all chunks selected, canCompile is true`() {
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
    }

    @Test
    fun `initially workChunk is the first chunk`() {
        val chapterPageViewModel = find<ChapterPageViewModel>()

        chapterPageViewModel.setWorkChunk()
        Assert.assertFalse(chapterPageViewModel.noTakesProperty.value)
        Assert.assertNull(chapterPageViewModel.workChunkProperty.value)

        val takeRelay = ReplayRelay.create<Take>()
        val mockedAudio = mock<AssociatedAudio> { on { takes } doReturn takeRelay }
        val mockedChunk = mock<Chunk> { on { audio } doReturn mockedAudio }
        val cardData = mock<CardData> {
            on { chunkSource } doReturn mockedChunk
            on { sort } doReturn 42
        }
        val cardData2 = mock<CardData> {
            on { chunkSource } doReturn mockedChunk
            on { sort } doReturn 100
        }

        chapterPageViewModel.filteredContent.setAll(cardData, cardData2)
        chapterPageViewModel.setWorkChunk()
        Assert.assertTrue(chapterPageViewModel.noTakesProperty.value)
        Assert.assertEquals(42, chapterPageViewModel.workChunkProperty.value.sort)
    }

    @Test
    fun `when first chunk has takes, workChunk is the second chunk`() {
        val chapterPageViewModel = find<ChapterPageViewModel>()

        chapterPageViewModel.setWorkChunk()
        Assert.assertFalse(chapterPageViewModel.noTakesProperty.value)
        Assert.assertNull(chapterPageViewModel.workChunkProperty.value)

        val mockedTake = mock<Take> {
            on {deletedTimestamp } doReturn mock()
        }
        val selectedMock = BehaviorRelay.createDefault(TakeHolder(mockedTake))
        val mockedAudio = mock<AssociatedAudio> {
            on { getAllTakes() } doReturn arrayOf(mockedTake)
            on { selected } doReturn selectedMock
        }
        val mockedChunk = mock<Chunk> { on { audio } doReturn mockedAudio }
        val cardData = mock<CardData> {
            on { chunkSource } doReturn mockedChunk
            on { sort } doReturn 42
        }
        val takeRelay2 = ReplayRelay.create<Take>()
        val mockedAudio2 = mock<AssociatedAudio> { on { takes } doReturn takeRelay2 }
        val mockedChunk2 = mock<Chunk> { on { audio } doReturn mockedAudio2 }
        val cardData2 = mock<CardData> {
            on { chunkSource } doReturn mockedChunk2
            on { sort } doReturn 100
        }

        chapterPageViewModel.filteredContent.setAll(cardData, cardData2)
        chapterPageViewModel.setWorkChunk()
        Assert.assertFalse(chapterPageViewModel.noTakesProperty.value)
        Assert.assertEquals(100, chapterPageViewModel.workChunkProperty.value.sort)
    }
}
