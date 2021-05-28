package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Maybe
import io.reactivex.Single
import javafx.beans.property.SimpleObjectProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.collections.DeleteProject
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.enums.SlugsEnum
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TranslationCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.WorkbookPage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.book.BookSelection
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.SourceLanguageSelection
import tornadofx.*
import javax.inject.Inject
import javax.inject.Provider

class HomePageViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(HomePageViewModel::class.java)

    @Inject lateinit var collectionRepo: ICollectionRepository
    @Inject lateinit var workbookRepo: IWorkbookRepository
    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var deleteProjectProvider: Provider<DeleteProject>
    @Inject lateinit var resourceMetadataRepository: IResourceMetadataRepository
    @Inject lateinit var preferencesRepository: IAppPreferencesRepository

    private val workbookDataStore: WorkbookDataStore by inject()
    private val navigator: NavigationMediator by inject()

    val translationModels = observableListOf<TranslationCardModel>()
    val resumeBookProperty = SimpleObjectProperty<Workbook>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun loadResumeBook() {
        resumeBookProperty.set(null)
        preferencesRepository.resumeProjectId()
            .doOnError { logger.debug("Error in resumeProjectId: $it") }
            .subscribe { id ->
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

    fun loadTranslations() {
        resourceMetadataRepository
            .getAllTargets()
            .map {
                it.filter { metadata -> metadata.identifier == SlugsEnum.ULB.slug }
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in loading target translations", e)
            }
            .subscribe { retrieved ->
                val translations = retrieved
                    .map(::mapToTranslationCardModel)
                    .sortedWith(translationCardModelSorter())
                translationModels.setAll(translations)
            }
    }

    fun clearSelectedProject() {
        workbookDataStore.activeWorkbookProperty.set(null)
    }

    fun createProject() {
        navigator.dock<BookSelection>()
    }

    fun createTranslation() {
        navigator.dock<SourceLanguageSelection>()
    }

    fun selectProject(workbook: Workbook) {
        setResumeBook(workbook)
        workbookDataStore.activeWorkbookProperty.set(workbook)
        workbook.target.resourceMetadata.let(workbookDataStore::setProjectFilesAccessor)
        navigator.dock<WorkbookPage>()
    }

    private fun getSource(target: ResourceMetadata): Maybe<ResourceMetadata> {
        return resourceMetadataRepository.getSource(target)
    }

    private fun getProjects(target: ResourceMetadata): Single<List<Workbook>> {
        return workbookRepo.getProjects(target)
    }

    private fun mapToTranslationCardModel(target: ResourceMetadata): TranslationCardModel {
        val source = getSource(target).blockingGet()
        val projects = getProjects(target)
            .blockingGet()
            .sortedBy { book -> book.source.sort }
            .toObservable()

        return TranslationCardModel(
            source.language,
            target.language,
            projects
        )
    }

    private fun translationCardModelSorter(): Comparator<TranslationCardModel> {
        return compareBy<TranslationCardModel> { translation ->
            translation.sourceLanguage.slug
        }.thenComparing { translation ->
            translation.targetLanguage.slug
        }
    }

    private fun setResumeBook(workbook: Workbook) {
        preferencesRepository
            .setResumeProjectId(workbook.target.collectionId)
            .subscribe()
    }
}
