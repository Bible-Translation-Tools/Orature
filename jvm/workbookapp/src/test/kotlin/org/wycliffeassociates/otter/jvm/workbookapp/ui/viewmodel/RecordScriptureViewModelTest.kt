//package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel
//
//import com.jakewharton.rxrelay2.ReplayRelay
//import java.io.File
//import java.time.LocalDate
//import javafx.beans.property.SimpleObjectProperty
//import org.junit.Test
//import org.wycliffeassociates.otter.common.data.primitives.ContentType
//import org.wycliffeassociates.otter.common.data.primitives.MimeType
//import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
//import org.wycliffeassociates.otter.common.data.workbook.Chunk
//import org.wycliffeassociates.otter.common.data.workbook.Take
//import org.wycliffeassociates.otter.common.data.workbook.TakeHolder
//import org.wycliffeassociates.otter.common.data.workbook.TextItem
//import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
//import tornadofx.*
//
//class RecordScriptureViewModelTest {
//
//    private val activeChunk = Chunk(
//        sort = 1,
//        audio = createAssociatedAudio(),
//        textItem = TextItem("Chunk 1", MimeType.USFM),
//        start = 1,
//        end = 1,
//        contentType = ContentType.TEXT,
//        resources = listOf(),
//        label = "Chunk"
//    )
//
//    private var selectedTake: Take?
//        get() = activeChunk.audio.selected.value?.value
//        set(take) {
//            activeChunk.audio.selected.accept(TakeHolder(take))
//        }
//
//    var selectedTakeProperty = SimpleObjectProperty<Take?>()
//    init {
//        activeChunk.audio.selected.value?.value?.getProperty(RecordScriptureViewModelTest::selectedTakeProperty)
//    }
//
//    private fun createAssociatedAudio() = AssociatedAudio(ReplayRelay.create<Take>())
//
//    @Test
//    fun test() {
//        val take1 = Take(
//            "testTake1.wav",
//            File("testTake1.wav"),
//            1,
//            MimeType.WAV,
//            LocalDate.now()
//        )
//        val take2 = Take(
//            "testTake2.wav",
//            File("testTake2.wav"),
//            2,
//            MimeType.WAV,
//            LocalDate.now()
//        )
//
//        selectedTakeProperty.onChangeAndDoNow {
//            println("From on change: ${selectedTake?.name}")
//        }
//
//        selectedTake = take1
//        println("Hardcode 1: ${selectedTake?.name}")
//
//        selectedTake = take2
//        println("Hardcode 2: ${selectedTake?.name}")
//    }
//}
