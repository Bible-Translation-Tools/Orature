package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.collections.CreateTranslation
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.TargetLanguageSelection
import tornadofx.*
import javax.inject.Inject

class TranslationViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(TranslationViewModel::class.java)

    @Inject lateinit var languageRepo: ILanguageRepository
    @Inject lateinit var collectionRepo: ICollectionRepository
    @Inject lateinit var resourceMetadataRepository: IResourceMetadataRepository
    @Inject lateinit var creationUseCase: CreateTranslation

    private val navigator: NavigationMediator by inject()

    val sourceLanguages = SortedFilteredList<Language>()
    val targetLanguages = SortedFilteredList<Language>()

    val selectedSourceLanguageProperty = SimpleObjectProperty<Language>()
    val selectedTargetLanguageProperty = SimpleObjectProperty<Language>()

    val showProgressProperty = SimpleBooleanProperty(false)

    private val sourceTranslations = mutableListOf<ResourceMetadata>()
    val existingLanguages = observableListOf<Language>()
    private val selectedSourceCollectionProperty = SimpleObjectProperty<ResourceMetadata>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        selectedSourceLanguageProperty.onChange { language ->
            language?.let {
                selectedSourceCollectionProperty.set(
                    sourceTranslations.singleOrNull { it.language == language }
                )
                navigator.dock<TargetLanguageSelection>()
            }
        }

        selectedTargetLanguageProperty.onChange {
            it?.let {
                createTranslation()
            }
        }
    }

    private fun createTranslation() {
        val source = selectedSourceCollectionProperty.value
        val language = selectedTargetLanguageProperty.value

        if (source != null && language != null) {
            showProgressProperty.set(true)
            creationUseCase
                .create(selectedSourceCollectionProperty.value, selectedTargetLanguageProperty.value)
                .doOnError { e ->
                    logger.error("Error in creating a translation for collection: $source", e)
                }
                .subscribe { _ ->
                    showProgressProperty.set(false)
                    Platform.runLater { navigator.home() }
                }
        }
    }

    fun reset() {
        sourceTranslations.clear()
        sourceLanguages.clear()
        targetLanguages.clear()
        existingLanguages.clear()
        selectedSourceCollectionProperty.set(null)
        selectedSourceLanguageProperty.set(null)
        selectedTargetLanguageProperty.set(null)
    }

    fun loadSourceLanguages() {
        sourceLanguages.clear()
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
                sourceTranslations.addAll(collections)
                sourceLanguages.addAll(collections.map { it.language })
            }
    }

    fun loadTargetLanguages() {
        targetLanguages.clear()
        languageRepo
            .getAll()
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error initializing target languages", e)
            }
            .subscribe { retrieved ->
                targetLanguages.addAll(retrieved)
                loadTranslations()
            }
    }

    private fun loadTranslations() {
        val source = selectedSourceCollectionProperty.value
        resourceMetadataRepository
            .getTargets(source)
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error loading translations", e)
            }
            .subscribe { translations ->
                existingLanguages.setAll(
                    translations.map { it.language }.intersect(targetLanguages)
                )
            }
    }
}
