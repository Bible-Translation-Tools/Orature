package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel

import javafx.collections.FXCollections
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import io.reactivex.disposables.CompositeDisposable
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.Recordable
import tornadofx.*
import java.util.concurrent.Callable

class RecordableTabViewModel(
    val labelProperty: SimpleStringProperty
) {
    val recordableProperty = SimpleObjectProperty<Recordable?>()
    var recordable by recordableProperty

    private val disposables = CompositeDisposable()

    val takesList = FXCollections.observableArrayList<Take>()

    init {
        recordableProperty.onChange { item ->
            clearDisposables()
            item?.let {
                loadTakes()
            }
        }
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        clearDisposables()
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    fun loadTakes() {
        takesList.clear()
        recordable?.audio?.takes
            ?.subscribe {
                Platform.runLater {
                    takesList.add(it)
                    takesList.removeOnDeleted(it)
                }
            }?.let { disposables.add(it) }
    }

    private fun ObservableList<Take>.removeOnDeleted(take: Take) {
        take.deletedTimestamp.subscribe { dateHolder ->
            if (dateHolder.value != null) {
                this.remove(take)
            }
        }.let { disposables.add(it) }
    }

    fun getFormattedTextBinding() = Bindings.createStringBinding(
        Callable { getFormattedText() },
        recordableProperty
    )

    private fun getFormattedText(): String? = recordable?.textItem?.text
}