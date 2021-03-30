package org.wycliffeassociates.otter.jvm.controls.breadcrumbs

import javafx.scene.layout.HBox
import tornadofx.*

class BreadcrumbBar: HBox() {

    private val items = observableListOf<BreadCrumb>()

    init {
        importStylesheet(javaClass.getResource("/css/breadcrumb-bar.css").toExternalForm())
        styleClass.setAll("breadcrumb-bar")

        items.onChange {
            children.clear()

            it.list.forEach { item ->
                item.isActiveProperty.set(false)
                children.add(item)
            }

            it.list.lastOrNull()?.isActiveProperty?.set(true)
        }
    }

    fun addItem(item: BreadCrumb) {
        if (items.contains(item).not()) {
            items.add(item)
        }
        removeItemAfter(item)
    }

    fun removeItem(item: BreadCrumb) {
        items.remove(item)
    }

    private fun removeItemAfter(item: BreadCrumb) {
        val fromIndex = items.indexOf(item) + 1
        items.remove(fromIndex, items.size)
    }
}
