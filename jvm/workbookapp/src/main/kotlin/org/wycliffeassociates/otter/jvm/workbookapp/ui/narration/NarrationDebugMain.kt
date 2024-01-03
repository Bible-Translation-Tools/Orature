/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.Single
import javafx.scene.layout.Priority
import javafx.stage.Stage
import org.wycliffeassociates.otter.common.OratureInfo
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.DirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.DatabaseInitializer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import javax.inject.Inject


class NarrationRootView : View() {
    override val root = borderpane { center<Workspace>() }

    init {
        workspace.header.removeFromParent()
        workspace.root.vgrow = Priority.ALWAYS
    }
}

class NarrationDebugApp : App(NarrationRootView::class), IDependencyGraphProvider {

    override val dependencyGraph = DaggerAppDependencyGraph.builder().build()
    val workbookDataStore by inject<WorkbookDataStore>()

    @Inject
    lateinit var configureAudioSystem: ConfigureAudioSystem

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    private val chunkText = listOf(
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

    init {
        DatabaseInitializer(
            DirectoryProvider(OratureInfo.SUITE_NAME)
        ).initialize()
        dependencyGraph.inject(this)

        mockWorkbook()

        directoryProvider.cleanTempDirectory()
        configureAudioSystem.configure()

        tryImportStylesheet(resources["/css/theme/dark-theme.css"])
        tryImportStylesheet(resources["/css/theme/light-theme.css"])
        tryImportStylesheet(resources["/css/common.css"])
        tryImportStylesheet(resources["/css/control.css"])
        tryImportStylesheet(resources["/css/app-bar.css"])
    }

    override fun start(stage: Stage) {
        super.start(stage)
        stage.height = 760.0
        stage.width = 1024.0
        stage.scene.root.addClass(ColorTheme.LIGHT.styleClass)

        workspace.dock<NarrationPage>()
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
        mockSourceAudioAccessor(workbook)
        mockTranslation(workbook)

        workbookDataStore.activeWorkbookProperty.set(workbook)
        workbookDataStore.activeChapterProperty.set(target.chapters.blockingFirst())
    }

    private fun mockBook(book: Book) {
        every { book.slug } returns "mat"
        every { book.title } returns "Matthew"
        every { book.chapters } returns mockChapters()
        every { book.language } returns mockLanguage()
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
            every { chapter.sort } returns i
            every { chapter.text } returns "Chapter Text $i"
            every { chapter.title } returns i.toString()
            every { chapter.label } returns "chapter"
            every { chapter.getDraft() } returns mockChunks()
            every { chapter.chunkCount } returns Single.just(10)
            every { chapter.audio } returns mockAudio()
            every { chapter.chunkCount } returns Single.just(10)
            every { chapter.getSelectedTake() } returns chapter.audio.selected.value?.value
            chapters.add(chapter)
        }
        return Observable.fromIterable(chapters)
    }

    private fun mockLanguage(): Language {
        return Language(
            "en",
            "English",
            "English",
            "ltr",
            true,
            "Europe"
        )
    }

    private fun mockChunks(): ReplayRelay<Chunk> {
        val chunks = mutableListOf<Chunk>()
        for (i in 1..10) {
            val item = TextItem(chunkText[i - 1], MimeType.USFM)
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

    private fun mockAudio(): AssociatedAudio {
        val audio = mockk<AssociatedAudio>()
        val take = mockTake()

        val takes = ReplayRelay.create<Take>()
        take?.let { takes.apply { this.accept(it) } }

        val selected = take?.let {
            BehaviorRelay.createDefault(TakeHolder(take))
        } ?: BehaviorRelay.createDefault(TakeHolder.empty)

        every { audio.takes } returns takes
        every { audio.selected } returns selected
        every { audio.selectTake(any()) } returns Unit

        return audio
    }

    private fun mockTake(): Take? {
        val userHomeDir = File(System.getProperty("user.home"), "narration").also {
            if (!it.exists()) it.mkdirs()
        }
        val takeFile = File(userHomeDir, "narration.wav")

        return if (takeFile.exists()) {
            val take = mockk<Take>()
            every { take.file } returns takeFile
            every { take.name } returns takeFile.name
            every { take.number } returns 1
            every { take.format } returns MimeType.WAV
            take
        } else null
    }

    private fun mockProjectFileAccessor(workbook: Workbook) {
        val projectFileAccessor = mockk<ProjectFilesAccessor>()
        every { projectFileAccessor.getChapterText(any(), any(), any()) } returns chunkText

        mockProjectAudioDir(projectFileAccessor)

        every { workbook.projectFilesAccessor } returns projectFileAccessor
    }

    private fun mockSourceAudioAccessor(workbook: Workbook) {
        val sourceAudioAccessor = mockk<SourceAudioAccessor>()
        every { sourceAudioAccessor.getChapter(any(), any()) } returns null
        every { workbook.sourceAudioAccessor } returns sourceAudioAccessor
    }

    private fun mockTranslation(workbook: Workbook) {
        val translation = AssociatedTranslation(
            BehaviorRelay.createDefault(1.0),
            BehaviorRelay.createDefault(1.0)
        )
        every { workbook.translation } returns translation
    }

    private fun mockProjectAudioDir(accessor: ProjectFilesAccessor) {
        val narrationDir = File(System.getProperty("user.home"), "narration").also {
            if (!it.exists()) it.mkdirs()
        }
        every { accessor.audioDir } returns narrationDir
        every { accessor.getChapterAudioDir(any(), any()) } returns accessor.audioDir
    }
}

fun main() {
    launch<NarrationDebugApp>()
}