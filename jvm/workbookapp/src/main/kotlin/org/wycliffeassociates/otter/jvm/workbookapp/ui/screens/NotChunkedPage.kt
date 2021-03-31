package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javax.inject.Inject
import javax.inject.Provider
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.domain.plugins.PluginParameters
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class NotChunkedPage : Fragment() {

    val workbookDataStore: WorkbookDataStore by inject()
    @Inject
    lateinit var launchPluginProvider: Provider<LaunchPlugin>
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
                (app as OtterApp).dependencyGraph.inject(this@NotChunkedPage)
                launchPlugin()
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

    fun launchPlugin() {
        val workbook = workbookDataStore.activeWorkbookProperty.value
        val chapter = workbookDataStore.activeChapterProperty.value
        val sourceAudio = workbook.sourceAudioAccessor.getChapter(chapter.sort)
        val file = sourceAudio!!.file
        val wav = WavFile(file)
        (wav.metadata.getCues() as MutableList).clear()
        wav.update()
        val params = PluginParameters(
            languageName = "en",
            bookTitle = "jas",
            chapterLabel = chapter.sort.toString(),
            chapterNumber = chapter.sort,
            sourceChapterAudio = file,
            verseTotal = 30
        )
        launchPluginProvider
            .get()
            .launchPlugin(PluginType.MARKER, file, params)
            .subscribe { _ ->
                val wav = WavFile(sourceAudio!!.file)
                wav.update()
                val chunks = wav.metadata.getCues().size

                commitChunks(chunks)
            }
    }

    fun commitChunks(chunkCount: Int) {
        val chapter = workbookDataStore.activeChapterProperty.value
        val max = chunkCount
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
