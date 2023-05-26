package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDescriptorRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.controls.model.ProjectGroupKey
import org.wycliffeassociates.otter.jvm.controls.model.TranslationCardModel2
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.ViewModel
import tornadofx.observableListOf
import tornadofx.toObservable
import javax.inject.Inject

class HomePageViewModel2 : ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var workbookRepo: IWorkbookRepository
    @Inject
    lateinit var workbookDescriptorRepo: IWorkbookDescriptorRepository

    val translationModels2 = observableListOf<TranslationCardModel2>()
    val allBooks = observableListOf<WorkbookDescriptor>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun loadProjects(): Single<List<WorkbookDescriptor>> {
        translationModels2.clear()
        return workbookDescriptorRepo.getAll()
            .observeOnFx()
            .doOnSuccess { books ->
                updateBookList(books)
            }
    }

    private fun updateBookList(books: List<WorkbookDescriptor>) {
        val projectGroups = books.groupBy {
            ProjectGroupKey(it.sourceLanguage.slug, it.targetLanguage.slug, it.mode)
        }
        projectGroups.map {
            val book = it.value.first()
            val mostRecentBook = it.value.maxByOrNull { it.lastModified?.nano ?: -1 }
            TranslationCardModel2(
                book.sourceLanguage,
                book.targetLanguage,
                book.mode,
                mostRecentBook?.lastModified,
                it.value.toObservable()
            )
        }.let {
            translationModels2.addAll(it)
        }
    }
}