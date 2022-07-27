package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class OQuAView : View() {
    private val homeView = HomeView()
    private val projectView = ProjectView()
    private val chapterView = ChapterView()

    private val wbDataStore: WorkbookDataStore by inject()

    private val view = SimpleObjectProperty<View>(homeView)
    private val viewRoot = Bindings.createObjectBinding({ view.value?.root }, view)

    init {
        javaClass.getResource("/css/oqua.css")?.let { tryImportStylesheet(it.toExternalForm()) }

        wbDataStore.activeWorkbookProperty.onChange { updateView() }
        wbDataStore.activeChapterProperty.onChange { updateView() }

        view.value?.onDock()
    }

    private fun updateView() {
        view.value?.onUndock()

        if (wbDataStore.activeWorkbookProperty.value == null) {
            view.set(homeView)
        } else if (wbDataStore.activeChapterProperty.value == null) {
            view.set(projectView)
        } else {
            view.set(chapterView)
        }

        view.value?.onDock()
    }

    override val root = borderpane {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        top<NavBar>()
        centerProperty().bind(viewRoot)
    }
}