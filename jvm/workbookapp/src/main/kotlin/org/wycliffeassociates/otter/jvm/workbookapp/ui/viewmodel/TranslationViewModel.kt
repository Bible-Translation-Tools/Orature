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
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.collections.CreateTranslation
import org.wycliffeassociates.otter.common.domain.collections.DeleteTranslation
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TranslationCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.TargetLanguageSelection
import tornadofx.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TranslationViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(TranslationViewModel::class.java)

    @Inject
    lateinit var languageRepo: ILanguageRepository
    @Inject
    lateinit var collectionRepo: ICollectionRepository
    @Inject
    lateinit var resourceMetadataRepository: IResourceMetadataRepository
    @Inject
    lateinit var creationUseCase: CreateTranslation
    @Inject
    lateinit var deleteUseCase: DeleteTranslation

    private val navigator: NavigationMediator by inject()

    val sourceLanguages = SortedFilteredList<Language>()
    val targetLanguages = SortedFilteredList<Language>()

    val selectedSourceLanguageProperty = SimpleObjectProperty<Language>()
    val selectedTargetLanguageProperty = SimpleObjectProperty<Language>()

    val showProgressProperty = SimpleBooleanProperty(false)

    private val sourceResources = mutableListOf<ResourceMetadata>()
    val existingLanguages = observableListOf<Language>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        selectedSourceLanguageProperty.onChange { language ->
            language?.let {
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
        val source = selectedSourceLanguageProperty.value
        val target = selectedTargetLanguageProperty.value

        if (source != null && target != null) {
            showProgressProperty.set(true)
            creationUseCase
                .create(source, target)
                .doOnError { e ->
                    logger.error("Error in creating a translation for collection: $source", e)
                }
                .subscribe { _ ->
                    showProgressProperty.set(false)
                    Platform.runLater { navigator.home() }
                }
        }
    }

    fun deleteTranslation(
        translation: TranslationCardModel,
        callback: () -> Unit = {}
    ) {
        deleteUseCase
            .delete(translation.sourceLanguage, translation.targetLanguage)
            .doOnError {
                logger.error(
                    "Error while removing translation: " +
                            "${translation.sourceLanguage.id} - ${translation.targetLanguage.id}"
                )
            }
            .doOnComplete(callback)
            .subscribe()
    }

    fun reset() {
        sourceResources.clear()
        sourceLanguages.clear()
        targetLanguages.clear()
        existingLanguages.clear()
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
                sourceResources.addAll(collections)
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
        languageRepo
            .getAllTranslations()
            .map { list ->
                list.filter {
                    it.source == selectedSourceLanguageProperty.value
                }
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error loading translations", e)
            }
            .subscribe { translations ->
                existingLanguages.setAll(
                    translations.map { it.target }.intersect(targetLanguages)
                )
            }
    }
}
