package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.jakewharton.rxrelay2.ReplayRelay
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import javafx.stage.Stage
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import javax.inject.Inject

class NarrationApp : App(NarrationView::class), IDependencyGraphProvider {

    override val dependencyGraph = DaggerAppDependencyGraph.builder().build()
    val workbookDataStore by inject<WorkbookDataStore>()

    @Inject
    lateinit var configureAudioSystem: ConfigureAudioSystem

    init {
        dependencyGraph.inject(this)

        mockWorkbook()
        configureAudioSystem.configure()

        tryImportStylesheet(resources["/css/theme/dark-theme.css"])
        tryImportStylesheet(resources["/css/theme/light-theme.css"])
        tryImportStylesheet(resources["/css/common.css"])
        tryImportStylesheet(resources["/css/control.css"])
        tryImportStylesheet(resources["/css/app-bar.css"])
    }

    override fun start(stage: Stage) {
        super.start(stage)
        stage.height = 600.0
        stage.width = 800.0
        stage.scene.root.addClass(org.wycliffeassociates.otter.common.data.ColorTheme.LIGHT.styleClass)
    }

    private fun mockWorkbook() {
        val workbook = mockk<Workbook>()
        val target = mockk<Book>()
        val source = mockk<Book>()

        mockBook(target)
        mockBook(source)

        every { workbook.target } returns target
        every { workbook.source } returns source

        mockProjectFileAccessor(workbook)

        workbookDataStore.activeWorkbookProperty.set(workbook)
        workbookDataStore.activeChapterProperty.set(target.chapters.blockingFirst())
    }

    private fun mockBook(book: Book) {
        every { book.slug } returns "mat"
        every { book.title } returns "Matthew"
        every { book.chapters } returns mockChapters()
        mockResourceMetadata(book)
    }

    private fun mockResourceMetadata(book: Book) {
        every { book.resourceMetadata } returns mockk {
            every { language } returns mockk {
                every { name } returns "English"
                every { license } returns "Public Domain"
            }
        }
    }

    private fun mockChapters(): Observable<Chapter> {
        val chapters = mutableListOf<Chapter>()
        for (i in 1..28) {
            val chapter = mockk<Chapter>()
            every { chapter.text } returns "Chapter Text $i"
            every { chapter.title } returns i.toString()
            every { chapter.getDraft() } returns mockChunks()
            chapters.add(chapter)
        }
        return Observable.fromIterable(chapters)
    }

    private fun mockChunks(): ReplayRelay<Chunk> {
        val chunkText = listOf(
            "In the beginning, God created the heavens and the earth.",
            "The earth was without form and empty. Darkness was upon the surface of the deep. The Spirit of God was moving above the surface of the waters.",
            "God said, \"Let there be light,\" and there was light.",
            "God saw the light, that it was good. He divided the light from the darkness.",
            "God called the light \"day,\" and the darkness he called \"night.\" And there was evening and there was morning, the first day.",
            "God said, \"Let there be an expanse between the waters, and let it divide the waters from the waters.\"",
            "God made the expanse and divided the waters which were under the expanse from the waters which were above the expanse. It was so.",
            "God called the expanse \"sky.\" And there was evening and there was morning, the second day.",
            "God said, \"Let the waters under the sky be gathered together to one place, and let the dry land appear.\" It was so.",
            "God called the dry land \"earth,\" and the gathered waters he called \"seas.\" He saw that it was good",
        )

        val chunks = mutableListOf<Chunk>()
        for (i in 1..10) {
            val item = TextItem(chunkText[i-1], MimeType.USFM)
            val chunk = mockk<Chunk>()
            every { chunk.sort } returns i
            every { chunk.label } returns "verse"
            every { chunk.textItem } returns item
            every { chunk.title } returns "$i"
            every { chunk.start } returns i
            every { chunk.end } returns i
            chunks.add(chunk)
        }
        return ReplayRelay.create<Chunk>().apply { chunks.forEach { this.accept(it) } }
    }

    private fun mockProjectFileAccessor(workbook: Workbook) {
        val projectFileAccessor = mockk<ProjectFilesAccessor>()

        mockProjectAudioDir(projectFileAccessor)

        every { workbook.projectFilesAccessor } returns projectFileAccessor
    }

    private fun mockProjectAudioDir(accessor: ProjectFilesAccessor) {
        val userHomeDir = File(System.getProperty("user.home"))
        every { accessor.audioDir } returns userHomeDir
    }
}

fun main() {
    launch<NarrationApp>()
}