package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class HomeViewModel: ViewModel() {
    private val wbDataStore: WorkbookDataStore by inject()
    private val workbookRepo = (app as OQuAApp).dependencyGraph.injectWorkbookRepository()

    val tCards = observableListOf<TranslationCard>()

    fun dock() {
        getTranslations()
        wbDataStore.activeWorkbookProperty.set(null)
    }

    fun undock() {
        clearTCards()
    }

    private fun getTranslations() {
        workbookRepo
            .getProjects()
            .toObservable()
            .flatMap {
                Observable.fromIterable(it.filter{ workbook ->
                    workbookHasAudio(workbook)
                })
            }
            .map { workbook -> TranslationCard.mapFromWorkbook(workbook) }
            .observeOnFx()
            .subscribe { tCard ->
                val existingSource = tCards.find { card -> card == tCard }
                (existingSource?.projects?.addAll(tCard.projects)) ?: tCards.add(tCard)

                tCards.forEach { card -> card.sortProjects() }
                tCards.sortByDescending { card -> card.projects.size }
            }
    }

    private fun clearTCards() {
        tCards.setAll()
    }

    private fun workbookHasAudio(workbook: Workbook): Boolean {
        return workbook
            .target
            .chapters
            .toList()
            .blockingGet()
            .any { it.hasAudio() }
    }
}