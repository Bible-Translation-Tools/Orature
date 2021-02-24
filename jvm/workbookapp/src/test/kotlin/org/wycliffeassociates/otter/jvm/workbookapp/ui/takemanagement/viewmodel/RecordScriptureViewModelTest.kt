package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel

import com.jakewharton.rxrelay2.ReplayRelay
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Parent
import javafx.scene.layout.Region
import org.junit.Test
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.data.model.MimeType
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.DependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.AppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import tornadofx.*
import java.io.File
import java.time.LocalDate

class RecordScriptureViewModelTest {

    private val activeChunk = Chunk(
        sort = 1,
        audio = createAssociatedAudio(),
        textItem = TextItem("Chunk 1", MimeType.USFM),
        start = 1,
        end = 1,
        contentType = ContentType.TEXT,
        resources = listOf(),
        label = "Chunk"
    )

    private var selectedTake: Take?
        get() = activeChunk.audio.selected.value?.value
        set(take) {
            activeChunk.audio.selected.accept(TakeHolder(take))
        }

    var selectedTakeProperty = SimpleObjectProperty<Take?>()
    init {
        activeChunk.audio.selected.value?.value?.getProperty(RecordScriptureViewModelTest::selectedTakeProperty)
    }

    private fun createAssociatedAudio() = AssociatedAudio(ReplayRelay.create<Take>())

    @Test
    fun test() {
        val take1 = Take(
            "testTake1.wav",
            File("testTake1.wav"),
            1,
            MimeType.WAV,
            LocalDate.now()
        )
        val take2 = Take(
            "testTake2.wav",
            File("testTake2.wav"),
            2,
            MimeType.WAV,
            LocalDate.now()
        )

        selectedTakeProperty.onChangeAndDoNow {
            println("From on change: ${selectedTake?.name}")
        }

        selectedTake = take1
        println("Hardcode 1: ${selectedTake?.name}")

        selectedTake = take2
        println("Hardcode 2: ${selectedTake?.name}")
    }
}
