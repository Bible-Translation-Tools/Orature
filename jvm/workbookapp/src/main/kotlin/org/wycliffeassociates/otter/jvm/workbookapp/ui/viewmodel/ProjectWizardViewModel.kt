package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import java.util.function.Predicate
import javax.inject.Inject

class ProjectWizardViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var creationUseCase: CreateProject
    @Inject
    lateinit var languageRepo: ILanguageRepository
    @Inject
    lateinit var collectionRepo: ICollectionRepository

    private val sourceLanguages = observableListOf<Language>()
    private val targetLanguages = observableListOf<Language>()
    private val filteredSourceLanguages = FilteredList(sourceLanguages)
    private val filteredTargetLanguage = FilteredList(targetLanguages)
    val sortedSourceLanguages = SortedList(filteredSourceLanguages)
    val sortedTargetLanguages = SortedList(filteredTargetLanguage)

    val selectedModeProperty = SimpleObjectProperty<ProjectMode>(null)
    val selectedSourceLanguageProperty = SimpleObjectProperty<Language>(null)
    val selectedTargetLanguageProperty = SimpleObjectProperty<Language>(null)

    val sourceLanguageSearchQueryProperty = SimpleStringProperty("")
    val targetLanguageSearchQueryProperty = SimpleStringProperty("")

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
                collections
                    .mapNotNull { collection -> collection.resourceContainer }
                    .distinctBy { it.language }
            }
            .doOnError { e ->
                logger.error("Error in initializing source languages", e)
            }
            .subscribe { collections ->
                sourceLanguages.setAll(collections.map { it.language })
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

    fun onLanguageSelected(language: Language, onNavigateBack: () -> Unit) {
        val sourceLanguage = selectedSourceLanguageProperty.value
        if (sourceLanguage != null) {
            logger.info("Creating project group: ${sourceLanguage.slug} - ${language.slug}")
            creationUseCase
                .createAllBooks(
                    sourceLanguage,
                    language,
                    selectedModeProperty.value
                )
                .observeOnFx()
                .subscribe {
                    reset()
                    onNavigateBack()
                }
        }
        else {
            // source language selected
            selectedSourceLanguageProperty.set(language)
        }
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

    private fun reset() {
        selectedModeProperty.set(null)
        selectedSourceLanguageProperty.set(null)
        selectedTargetLanguageProperty.set(null)
        sourceLanguageSearchQueryProperty.set("")
        targetLanguageSearchQueryProperty.set("")
    }
}