package org.wycliffeassociates.otter.jvm.app.ui.resources.viewmodel

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.utils.mapNotNull
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.ResourceGroupCardItemList
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.resourceGroupCardItem
import tornadofx.*

class ResourcesViewModel : ViewModel() {
    val activeWorkbookProperty = SimpleObjectProperty<Workbook>()
    val workbook: Workbook
        get() = activeWorkbookProperty.value

    val activeChapterProperty = SimpleObjectProperty<Chapter>()
    val chapter: Chapter
        get() = activeChapterProperty.value

    val activeResourceSlugProperty = SimpleStringProperty()
    val resourceSlug: String
        get() = activeResourceSlugProperty.value

    val resourceGroups: ResourceGroupCardItemList = ResourceGroupCardItemList(mutableListOf())

    fun loadResourceGroups() {
        chapter
            .children
            .startWith(chapter)
            .mapNotNull { resourceGroupCardItem(it, resourceSlug, onSelect = this::navigateToTakesPage) }
            .buffer(2) // Buffering by 2 prevents the list UI from jumping while groups are loading
            .subscribe {
                Platform.runLater {
                    resourceGroups.addAll(it)
                }
            }
    }

    private fun navigateToTakesPage(resource: Resource) {
        // TODO use navigator
    }
}