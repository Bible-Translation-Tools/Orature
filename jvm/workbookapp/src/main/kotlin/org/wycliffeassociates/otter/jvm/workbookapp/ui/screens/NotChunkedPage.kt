package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.geometry.Pos
import javafx.scene.control.TextField
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class NotChunkedPage : Fragment() {

    val workbookDataStore: WorkbookDataStore by inject()
    lateinit var input: TextField

    override val root = hbox {
        alignment = Pos.CENTER
        input = textfield {
            prefWidth = 50.0
            prefHeight = 50.0
        }
        button("Chunk") {
            prefWidth = 300.0
            action {
                val chapter = workbookDataStore.activeChapterProperty.value
                val max = input.text.toInt()
                val list = arrayListOf<Int>()
                for (i in 1..max) { list.add(i) }
                chapter.userChunkList!!.accept(list)
                chapter.chunks
                    .subscribeOn(Schedulers.computation())
                    .observeOnFx()
                    .subscribe(
                    {}, {},
                    {
                        workbookDataStore.activeChapterProperty.value.chunked = true
                        workspace.dockedComponent!!.replaceWith<ChapterPage>()
                    }
                )
            }
        }
        button("Skip") {
            prefWidth = 300.0
            action {
                val chapter = workbookDataStore.activeChapterProperty.value
                chapter.chunks
                    .subscribeOn(Schedulers.computation())
                    .observeOnFx()
                    .subscribe({}, {},
                    {
                        chapter.chunked = true
                        workspace.dockedComponent!!.replaceWith<ChapterPage>()
                    }
                )
            }
        }
    }
}
