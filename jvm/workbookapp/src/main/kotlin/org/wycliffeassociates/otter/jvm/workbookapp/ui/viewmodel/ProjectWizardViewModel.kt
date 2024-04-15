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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.collections.DeleteProject
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Predicate
import javax.inject.Inject

class ProjectWizardViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var creationUseCase: CreateProject
    @Inject
    lateinit var deleteProjectUseCase: DeleteProject
    @Inject
    lateinit var languageRepo: ILanguageRepository
    @Inject
    lateinit var collectionRepo: ICollectionRepository
    @Inject
    lateinit var importer: ImportProjectUseCase

    private val sourceLanguages = observableListOf<Language>()
    private val targetLanguages = observableListOf<Language>()
    private val filteredSourceLanguages = FilteredList(sourceLanguages)
    private val filteredTargetLanguage = FilteredList(targetLanguages)
    val sortedSourceLanguages = SortedList(filteredSourceLanguages)
    val sortedTargetLanguages = SortedList(filteredTargetLanguage)
    val existingLanguagePairs = observableListOf<Pair<Language, Language>>()

    val selectedModeProperty = SimpleObjectProperty<ProjectMode>(null)
    val selectedSourceLanguageProperty = SimpleObjectProperty<Language>(null)
    val selectedTargetLanguageProperty = SimpleObjectProperty<Language>(null)

    val sourceLanguageSearchQueryProperty = SimpleStringProperty("")
    val targetLanguageSearchQueryProperty = SimpleStringProperty("")
    private val projectDeleteCounter = AtomicInteger(0)

    private val disposableListeners = mutableListOf<ListenerDisposer>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        setupLanguageSearchListener(sourceLanguageSearchQueryProperty, filteredSourceLanguages, sortedSourceLanguages)
        setupLanguageSearchListener(targetLanguageSearchQueryProperty, filteredTargetLanguage, sortedTargetLanguages)
    }

    private fun setupLanguageSearchListener(
        queryStringProperty: ObservableValue<String>,
        filteredList: FilteredList<Language>,
        sortedList: SortedList<Language>
    ) {
        queryStringProperty.onChange { q ->
            val query = q?.trim()

            if (query.isNullOrEmpty()) {
                filteredList.predicate = Predicate { true }
                sortedList.comparator = compareBy<Language> { language -> language.slug }
            } else {
                filteredList.predicate = Predicate { language ->
                    language.slug.contains(query, true)
                        .or(language.name.contains(query, true))
                        .or(language.anglicizedName.contains(query, true))
                }

                val lowerQuery = query.lowercase()
                sortedList.comparator = compareByDescending<Language> { language -> language.slug == lowerQuery }
                    .thenByDescending { language -> language.name.lowercase() == lowerQuery }
                    .thenByDescending { language -> language.anglicizedName.lowercase() == lowerQuery }
            }
        }
    }

    fun loadSourceLanguages() {
        collectionRepo
            .getRootSources()
            .observeOnFx()
            .map { collections ->
                val sourceLanguagesFromRoot = collections
                    .mapNotNull { collection -> collection.resourceContainer }
                    .map { it.language }
                    .distinct()

                val availableGLs = languageRepo.getAvailableGatewaySources().blockingGet()
                sourceLanguagesFromRoot.union(availableGLs)
            }
            .doOnError { e ->
                logger.error("Error in initializing source languages", e)
            }
            .subscribe { languages ->
                sourceLanguages.setAll(languages)
                updateExistingLanguagePairs()
            }
    }

    fun loadTargetLanguages() {
        languageRepo
            .getAll()
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error initializing target languages", e)
            }
            .subscribe { retrieved ->
                targetLanguages.setAll(retrieved)
            }
    }

    fun onLanguageSelected(projectMode: ProjectMode, language: Language, onNavigateBack: () -> Unit) {
        val sourceLanguage = selectedSourceLanguageProperty.value
        if (sourceLanguage != null) {
            createProject(sourceLanguage, targetLanguage = language, onNavigateBack)
        }
        else {
            // source language selected
            selectedSourceLanguageProperty.set(language)
        }

        if (projectMode == ProjectMode.NARRATION) {
            createProject(language, language, onNavigateBack)
        }
    }

    private fun createProject(
        sourceLanguage: Language,
        targetLanguage: Language,
        onNavigateBack: () -> Unit
    ) {
        logger.info("Creating project group: ${sourceLanguage.name} - ${targetLanguage.name}")

        // check if source metadata exists for the requested language
        val sourceExists = collectionRepo.getRootSources().blockingGet()
            .any { it.resourceContainer!!.language == sourceLanguage }

        val prepareSource = if (!sourceExists) {
            logger.info("Sideloading source for language: ${sourceLanguage.name}")
            importer.sideloadSource(sourceLanguage)
        } else {
            Completable.complete()
        }

        creationUseCase
            .createAllBooks(
                sourceLanguage,
                targetLanguage,
                selectedModeProperty.value
            )
            .startWith(prepareSource) // must run after deletion and before creation
            .startWith(waitForProjectDeletionFinishes()) // this must run first
            .observeOnFx()
            .subscribe(
                {
                    logger.info("Project group created: ${sourceLanguage.name} - ${targetLanguage.name}")
                    existingLanguagePairs.add(Pair(sourceLanguage, targetLanguage))
                    reset()
                    onNavigateBack()
                },
                {
                    logger.error("Could not create project for ${sourceLanguage.name} - ${targetLanguage.slug} ${selectedModeProperty.value}")
                    find<HomePageViewModel2>().isLoadingProperty.set(false)
                }
            )
    }

    fun dock() {
        selectedModeProperty.onChangeWithDisposer {
            loadSourceLanguages()
        }.apply { disposableListeners.add(this) }

        selectedSourceLanguageProperty.onChangeWithDisposer { sourceLanguage ->
            when {
                sourceLanguage == null -> return@onChangeWithDisposer
                selectedModeProperty.value == ProjectMode.NARRATION -> {
                    targetLanguages.setAll(sourceLanguage)
                }
                else -> {
                    loadTargetLanguages()
                }
            }
        }.apply { disposableListeners.add(this) }
    }

    fun undock() {
        reset()
        disposableListeners.forEach { it.dispose() }
        disposableListeners.clear()
    }

    fun increaseProjectDeleteCounter() { projectDeleteCounter.incrementAndGet() }
    fun decreaseProjectDeleteCounter() { projectDeleteCounter.decrementAndGet() }

    /**
     * Blocks the execution of project creation until projects delete queue completes.
     */
    private fun waitForProjectDeletionFinishes(): Completable {
        val waitIntervalMillis = 100L
        return Observable.interval(waitIntervalMillis, TimeUnit.MILLISECONDS)
            .takeWhile { projectDeleteCounter.get() > 0 }
            .subscribeOn(Schedulers.io())
            .ignoreElements()
    }

    private fun reset() {
        selectedModeProperty.set(null)
        selectedSourceLanguageProperty.set(null)
        selectedTargetLanguageProperty.set(null)
        sourceLanguageSearchQueryProperty.set("")
        targetLanguageSearchQueryProperty.set("")
    }

    private fun updateExistingLanguagePairs() {
        val homePageViewModel = find<HomePageViewModel2>()
        val languagePairs = homePageViewModel.projectGroups.map {
            Pair(it.sourceLanguage, it.targetLanguage)
        }
        existingLanguagePairs.setAll(languagePairs)
    }
}
