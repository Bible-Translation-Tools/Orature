package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.layout.HBox
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class NarrationView(): View() {
    override val root = stackpane {
        borderpane {
            top<NarrationHeader>()

        }
    }
}

class NarrationHeader(): View() {
    private val viewModel by inject<NarrationHeaderViewModel>()

    override val root = hbox {
        narrationTitle(viewModel.titleProperty)

    }
}

class NarrationHeaderViewModel(): ViewModel() {
    private val workbookDataStore by inject<WorkbookDataStore>()

    val titleProperty = workbookDataStore.activeTitleBinding()
}

class NarrationTitle(val titleTextProperty: ObservableValue<String> = SimpleStringProperty()): HBox() {
    constructor(titleText: String): this(SimpleStringProperty(titleText))

    init {
        label(titleTextProperty)
    }
}

fun EventTarget.narrationTitle(
    titleTextProperty: ObservableValue<String>, op: NarrationTitle.() -> Unit = {}
) = NarrationTitle(titleTextProperty).attachTo(this, op)

fun EventTarget.narrationTitle(
    titleText: String, op: NarrationTitle.() -> Unit = {}
) = NarrationTitle(titleText).attachTo(this, op)