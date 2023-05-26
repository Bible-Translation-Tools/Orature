package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleObjectProperty
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
import javax.inject.Inject

class ProjectWizardViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var creationUseCase: CreateProject
    @Inject
    lateinit var languageRepo: ILanguageRepository
    @Inject
    lateinit var collectionRepo: ICollectionRepository

    val sourceLanguages = observableListOf<Language>()
    val targetLanguages = observableListOf<Language>()

    val selectedModeProperty = SimpleObjectProperty<ProjectMode>(null)
    val selectedSourceLanguageProperty = SimpleObjectProperty<Language>(null)
    val selectedTargetLanguageProperty = SimpleObjectProperty<Language>(null)

    private val disposableListeners = mutableListOf<ListenerDisposer>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

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
        }
    }

    fun loadSourceLanguages() {
        languageRepo
            .getGateway()
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error retrieving source languages.", e)
            }
            .subscribe { retrieved ->
                sourceLanguages.setAll(retrieved)
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
        if (selectedSourceLanguageProperty.value != null) {
            // target language selected, creates project group
            selectedTargetLanguageProperty.set(language)
            creationUseCase
                .createAllBooks(
                    selectedSourceLanguageProperty.value,
                    language,
                    selectedModeProperty.value
                )
                .observeOnFx()
                .subscribe {
                    resetWizard()
                    onNavigateBack()
                }
        }
        else {
            // source language selected
            selectedSourceLanguageProperty.set(language)
        }
    }

    fun resetWizard() {
        selectedModeProperty.set(null)
        selectedSourceLanguageProperty.set(null)
    }
}
