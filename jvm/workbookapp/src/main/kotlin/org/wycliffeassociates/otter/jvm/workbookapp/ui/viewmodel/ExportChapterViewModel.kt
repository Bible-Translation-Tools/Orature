/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.License
import org.wycliffeassociates.otter.common.domain.audio.AudioExporter
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ContributorCellData
import tornadofx.*
import java.io.File
import javax.inject.Inject

class ExportChapterViewModel : ViewModel() {
    @Inject
    lateinit var audioExporter: AudioExporter

    private val logger = LoggerFactory.getLogger(javaClass)
    private val workbookDataStore: WorkbookDataStore by inject()
    private val workbookPageViewModel: WorkbookPageViewModel by inject()
    private val chapterViewModel: ChapterPageViewModel by inject()

    val contributors = observableListOf<Contributor>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun export(outputDir: File) {
        chapterViewModel.selectedChapterTakeProperty.value?.let { take ->
            chapterViewModel.showExportProgressDialogProperty.set(true)

            val licenseTitle = workbookDataStore.workbook.target.resourceMetadata.license
            val license = License.get(licenseTitle)

            audioExporter.exportMp3(take.file, outputDir, license, contributors)
                .doOnError {
                    logger.error("Error while exporting chapter.", it)
                }
                .observeOnFx()
                .subscribe {
                    chapterViewModel.showExportProgressDialogProperty.set(false)
                }
        }
    }

    fun loadContributors() {
        contributors.setAll(workbookPageViewModel.contributors)
    }

    fun saveContributors() {
        Completable
            .fromAction {
                workbookDataStore.activeProjectFilesAccessor.setContributorInfo(
                    contributors.map { it.name }
                )
            }
            .observeOnFx()
            .doOnError {
                logger.error("Error saving contributor before export.", it)
            }
            .subscribe()
    }

    fun addContributor(name: String) {
        contributors.add(0, Contributor(name))
    }

    fun editContributor(data: ContributorCellData) {
        contributors[data.index] = Contributor(data.name)
    }

    fun removeContributor(index: Int) {
        contributors.removeAt(index)
    }
}