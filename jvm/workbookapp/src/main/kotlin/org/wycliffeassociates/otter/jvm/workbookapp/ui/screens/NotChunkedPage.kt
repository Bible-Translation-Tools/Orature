package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class NotChunkedPage : Fragment() {

    val workbookDataStore: WorkbookDataStore by inject()

    override val root = hbox {
        alignment = Pos.CENTER
        button("Chunk") { prefWidth = 300.0 }
        button("Skip") {
            prefWidth = 300.0
            action {
                workbookDataStore.activeChapterProperty.value.chunks.subscribe({}, {},
                    {
                        workbookDataStore.activeChapterProperty.value.chunked = true
                        workspace.navigateBack()
                        workspace.dock<ChapterPage>()
                    }
                )
            }
        }
    }
}
