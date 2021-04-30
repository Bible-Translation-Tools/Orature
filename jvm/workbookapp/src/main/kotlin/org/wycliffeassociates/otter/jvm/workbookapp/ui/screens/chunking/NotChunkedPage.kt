package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.application.ParametersImpl
import io.reactivex.schedulers.Schedulers
import java.text.MessageFormat
import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javax.inject.Inject
import javax.inject.Provider
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.domain.plugins.PluginParameters
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.ScopeVM
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ChapterPage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*

class NotChunkedPage : Fragment() {

    val scopeVM: ScopeVM by inject()
    val vm: ChunkingViewModel by inject()
    val workbookDataStore: WorkbookDataStore by inject()

    @Inject
    lateinit var launchPluginProvider: Provider<LaunchPlugin>
    lateinit var input: TextField

    override val root = vbox {
        alignment = Pos.CENTER
        maxHeight = 571.0
        maxWidth = 800.0
        spacing = 16.0
        add(
            resources.imageview("/images/notchunked.png").apply {
                prefHeight = 240.0
                prefWidth = 240.0
            }
        )
        text("Chunking Not Complete").apply {
            style {
                fontSize = 36.px
            }
        }
        text("Please chunk chapter # to continue. You may skip the chunking step if you prefer translating verse by verse.").apply {
            wrappingWidth = 500.0
            style {
                fontSize = 16.px
            }
        }
        button(
            "Chunk Chapter",
            graphic = FontIcon("gmi-arrow-forward").apply {
                iconSize = 20
                iconColor = Color.WHITE
            }
        ) {
            action {
                (app as OtterApp).dependencyGraph.inject(this@NotChunkedPage)
                launchPlugin()
            }
            style {
                underline = true
                fontSize = 20.px
                textFill = Color.WHITE
                backgroundColor += Paint.valueOf("#015AD9")
            }
        }
        button(
            "Skip",
            graphic = FontIcon("gmi-bookmark-outline").apply {
                iconSize = 20
                iconColor = Paint.valueOf("#015AD9")
            }
        ) {
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
            style {
                underline = true
                fontSize = 20.px
                backgroundColor += Color.WHITE
                textFill = Paint.valueOf("#015AD9")
            }
        }
        style {
            borderRadius += box(16.px)
            backgroundRadius += box(16.px)
            backgroundColor += Color.WHITE
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
            verseTotal = 500
        )
        vm.sourceAudio.set(wav)
        //     val wiz: ChunkingWizard = find()

        scopeVM.parametersProperty.set(constructParams(params))
        scopeVM.closeCallback =
            {
                val wav = WavFile(sourceAudio!!.file)
                wav.update()
                val chunks = wav.metadata.getCues().size
                commitChunks(chunks)
            }
        val wiz = find<ChunkingWizard> {
            onComplete {
                println("here")
            }
        }
        workspace.dock(wiz)

//        fire(PluginOpenedEvent(PluginType.MARKER, true))
//        launchPluginProvider
//            .get()
//            .launchPlugin(PluginType.MARKER, file, params)
//            .subscribe { _ ->
//                val wav = WavFile(sourceAudio!!.file)
//                wav.update()
//                val chunks = wav.metadata.getCues().size
//                commitChunks(chunks)
//            }
    }

    fun constructParams(pluginParameters: PluginParameters): Application.Parameters {
        val list = listOf(
            "--wav=${pluginParameters.sourceChapterAudio?.absolutePath}",
            "--language=${pluginParameters.languageName}",
            "--book=${pluginParameters.bookTitle}",
            "--chapter=${pluginParameters.chapterLabel}",
            "--chapter_number=${pluginParameters.chapterNumber}",
            "--marker_total=${pluginParameters.verseTotal}",
            (if (pluginParameters.chunkLabel != null) "--unit=${pluginParameters.chunkLabel}" else ""),
            (if (pluginParameters.chunkNumber != null) "--unit_number=${pluginParameters.chunkNumber}" else ""),
            (if (pluginParameters.resourceLabel != null) "--resource=${pluginParameters.resourceLabel}" else ""),
            "--chapter_audio=${pluginParameters.sourceChapterAudio?.absolutePath}",
            "--source_chunk_start=${pluginParameters.sourceChunkStart}",
            "--source_chunk_end=${pluginParameters.sourceChunkEnd}",
            "--source_text=${pluginParameters.sourceText}",
            "--action_title=${pluginParameters.actionText}",
            "--content_title=${
                MessageFormat.format(
                    FX.messages["bookChapterTitle"],
                    pluginParameters.bookTitle,
                    pluginParameters.chapterNumber
                )
            }"
        )
        return ParametersImpl(list)
    }


    fun commitChunks(chunkCount: Int) {
        val chapter = workbookDataStore.activeChapterProperty.value
        val max = chunkCount
        val list = arrayListOf<Int>()
        for (i in 1..max) {
            list.add(i)
        }
        chapter.userChunkList!!.accept(list)
        chapter.chunks
            .subscribeOn(Schedulers.computation())
            .observeOnFx()
            .subscribe(
                {}, {},
                {
                    fire(PluginClosedEvent(PluginType.MARKER))
                    workbookDataStore.activeChapterProperty.value.chunked = true
                    workspace.dockedComponent!!.replaceWith<ChapterPage>()
                }
            )
    }
}
