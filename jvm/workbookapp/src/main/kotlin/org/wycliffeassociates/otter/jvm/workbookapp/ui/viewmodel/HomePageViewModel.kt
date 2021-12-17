/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
import javafx.beans.property.SimpleObjectProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.collections.UpdateProject
import org.wycliffeassociates.otter.common.domain.collections.UpdateTranslation
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TranslationCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.WorkbookPage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.book.BookSelection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.SourceLanguageSelection
import tornadofx.*
import java.time.LocalDateTime
import javax.inject.Inject

const val NO_RESUMABLE_PROJECT = -1
const val NO_LAST_RESOURCE = ""

class HomePageViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(HomePageViewModel::class.java)

    @Inject lateinit var collectionRepo: ICollectionRepository
    @Inject lateinit var workbookRepo: IWorkbookRepository
    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var preferencesRepository: IAppPreferencesRepository
    @Inject lateinit var languageRepository: ILanguageRepository
    @Inject lateinit var updateTranslationUseCase: UpdateTranslation
    @Inject lateinit var updateProjectUseCase: UpdateProject

    private val workbookDataStore: WorkbookDataStore by inject()
    private val navigator: NavigationMediator by inject()

    val translationModels = observableListOf<TranslationCardModel>()
    val translations = observableListOf<Translation>()
    val resumeBookProperty = SimpleObjectProperty<Workbook>()
    private val settingsViewModel: SettingsViewModel by inject()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        settingsViewModel.refreshPlugins()
    }

    fun loadResumeBook() {
        resumeBookProperty.set(null)
        preferencesRepository.resumeProjectId()
            .doOnError { logger.debug("Error in resumeProjectId: $it") }
            .subscribe { id ->
                if (id != NO_RESUMABLE_PROJECT) {
                    collectionRepo
                        .getProject(id)
                        .flatMap { workbookRepo.getWorkbook(it) }
                        .onErrorComplete()
                        .observeOnFx()
                        .subscribe {
                            resumeBookProperty.set(it)
                        }
                }
            }
    }

    fun loadTranslations() {
        languageRepository
            .getAllTranslations()
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in loading target translations", e)
            }
            .subscribe { retrieved ->
                translations.setAll(retrieved)
                translationModels.setAll(
                    retrieved
                        .map(::mapToTranslationCardModel)
                        .sortedWith(translationCardModelComparator())
                )
            }
    }

    fun clearSelectedProject() {
        workbookDataStore.activeWorkbookProperty.value?.let {
            workbookRepo.closeWorkbook(it)
        }
        workbookDataStore.activeWorkbookProperty.set(null)
    }

    fun createProject(translation: TranslationCardModel) {
        val vm: BookWizardViewModel = find()
        vm.translationProperty.set(translation)
        navigator.dock<BookSelection>()
    }

    fun createTranslation() {
        navigator.dock<SourceLanguageSelection>()
    }

    fun selectProject(workbook: Workbook) {
        setResumeBook(workbook)
        workbookDataStore.activeWorkbookProperty.set(workbook)
        workbook.target.resourceMetadata.let(workbookDataStore::setProjectFilesAccessor)
        updateTranslationModifiedDate(workbook)
        updateWorkbookModifiedDate(workbook)

        navigator.dock<WorkbookPage>()
    }

    private fun mapToTranslationCardModel(translation: Translation): TranslationCardModel {
        val projects = workbookRepo.getProjects(translation).blockingGet()
        return TranslationCardModel(
            translation.source,
            translation.target,
            translation.modifiedTs,
            projects.sortedWith(
                compareByDescending<Workbook> { workbook ->
                    workbook.modifiedTs
                }.thenComparing { workbook ->
                    workbook.target.sort
                }
            ).asObservable()
        )
    }

    private fun translationCardModelComparator(): Comparator<TranslationCardModel> {
        return compareByDescending<TranslationCardModel> { translation ->
            translation.modifiedTs
        }.thenComparing { translation ->
            translation.sourceLanguage.slug
        }.thenComparing { translation ->
            translation.targetLanguage.slug
        }
    }

    private fun setResumeBook(workbook: Workbook) {
        updateLastResource(workbook.target.collectionId)
        preferencesRepository
            .setResumeProjectId(workbook.target.collectionId)
            .subscribe()
    }

    private fun updateLastResource(resumeBookId: Int) {
        preferencesRepository
            .resumeProjectId()
            .subscribe { id ->
                if (id != resumeBookId) {
                    // If selected project has changed, reset last resource tab
                    preferencesRepository.setLastResource(NO_LAST_RESOURCE).subscribe()
                }
            }
    }

    private fun updateTranslationModifiedDate(workbook: Workbook) {
        val translation = translations
            .singleOrNull {
                it.source.slug == workbook.source.language.slug
                        && it.target.slug == workbook.target.language.slug
            }

        translation?.let {
            it.modifiedTs = LocalDateTime.now()
            updateTranslationUseCase.update(it).subscribe()
        }
    }

    private fun updateWorkbookModifiedDate(workbook: Workbook) {
        val project = workbook.target.toCollection()
        project.modifiedTs = LocalDateTime.now()
        updateProjectUseCase.update(project).subscribe()
    }
}
