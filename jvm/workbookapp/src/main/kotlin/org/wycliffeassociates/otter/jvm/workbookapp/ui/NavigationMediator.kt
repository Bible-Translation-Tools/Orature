package org.wycliffeassociates.otter.jvm.workbookapp.ui

import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadcrumbBar
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import tornadofx.*

class NavigationMediator : Component(), ScopedInstance {

    val breadCrumbsBar = BreadcrumbBar()

    private val recorderBreadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["recording"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_MICROPHONE))
    }

    private val editorBreadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["recording"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_MICROPHONE))
    }

    private val markerBreadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["addMarkers"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_LINK_OFF))
    }

    init {
        subscribe<PluginOpenedEvent> {
            when(it.type) {
                PluginType.RECORDER -> breadCrumbsBar.addItem(recorderBreadCrumb)
                PluginType.EDITOR -> breadCrumbsBar.addItem(editorBreadCrumb)
                PluginType.MARKER -> breadCrumbsBar.addItem(markerBreadCrumb)
            }
        }
        subscribe<PluginClosedEvent> {
            when(it.type) {
                PluginType.RECORDER -> breadCrumbsBar.removeItem(recorderBreadCrumb)
                PluginType.EDITOR -> breadCrumbsBar.removeItem(editorBreadCrumb)
                PluginType.MARKER -> breadCrumbsBar.removeItem(markerBreadCrumb)
            }
        }
    }

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

    fun home() {
        dock(workspace.viewStack.first())
    }
}
