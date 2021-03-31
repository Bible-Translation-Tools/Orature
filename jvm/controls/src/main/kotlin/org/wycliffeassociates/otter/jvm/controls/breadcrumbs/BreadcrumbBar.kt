package org.wycliffeassociates.otter.jvm.controls.breadcrumbs

import javafx.beans.binding.Bindings
import javafx.scene.layout.HBox
import tornadofx.*
import java.util.concurrent.Callable

class BreadcrumbBar: HBox() {

    private val items = observableListOf<BreadCrumb>()

    init {
        importStylesheet(javaClass.getResource("/css/breadcrumb-bar.css").toExternalForm())
        styleClass.setAll("breadcrumb-bar")

        bindChildren(items) { breadcrumb ->
            breadcrumb.isActiveProperty.bind(
                Bindings.createBooleanBinding(
                    Callable { items.last() == breadcrumb },
                    items
                )
            )
            breadcrumb
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
