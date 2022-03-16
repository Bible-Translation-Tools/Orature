package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.domain.audio.AudioConverter
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ContributorCellData
import tornadofx.*
import java.io.File
import javax.inject.Inject

class ExportChapterViewModel : ViewModel() {
    @Inject
    lateinit var audioConverter: AudioConverter

    private val workbookPageViewModel: WorkbookPageViewModel by inject()
    private val chapterViewModel: ChapterPageViewModel by inject()

    val contributors = observableListOf<Contributor>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun export(outputDir: File) {
        chapterViewModel.selectedChapterTakeProperty.value?.let { take ->
            chapterViewModel.showExportProgressDialogProperty.set(true)

            val mp3Name = take.file.nameWithoutExtension + ".mp3"
            val mp3File = File(outputDir, mp3Name)
            audioConverter.wavToMp3(take.file, mp3File)
                .subscribeOn(Schedulers.io())
                .observeOnFx()
                .subscribe {
                    chapterViewModel.showExportProgressDialogProperty.set(false)
                }
        }
    }

    fun loadContributors() {
        contributors.setAll(workbookPageViewModel.contributors)
    }

    fun addContributor(name: String) {
        contributors.add(Contributor(name))
    }

    fun editContributor(data: ContributorCellData) {
        contributors[data.index] = Contributor(data.name)
    }

    fun removeContributor(index: Int) {
        contributors.removeAt(index)
    }
}