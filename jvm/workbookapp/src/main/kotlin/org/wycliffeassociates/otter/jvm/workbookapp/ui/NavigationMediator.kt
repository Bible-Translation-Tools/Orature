package org.wycliffeassociates.otter.jvm.workbookapp.ui

import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadcrumbBar
import tornadofx.*

class NavigationMediator : Component(), ScopedInstance {

    val breadCrumbsBar = BreadcrumbBar()

    inline fun <reified T : UIComponent> dock(breadCrumb: BreadCrumb? = null) {
        val view = find<T>()
        dock(view, breadCrumb)
    }

    fun dock(view: UIComponent, breadCrumb: BreadCrumb? = null) {
        breadCrumb?.let {
            breadCrumbsBar.addItem(it)
        }
        if (workspace.dockedComponent != view) {
            workspace.dock(view)
        }
    }

    fun back() {
        workspace.navigateBack()
    }

    fun forward() {
        workspace.navigateForward()
    }
}
