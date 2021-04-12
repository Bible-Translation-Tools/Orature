package org.wycliffeassociates.otter.jvm.workbookapp.ui

import javafx.beans.property.SimpleObjectProperty
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadcrumbBar
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import tornadofx.*

class NavigationMediator: Component(), ScopedInstance {

    val breadCrumbsBar = BreadcrumbBar()
    val dockedComponent: UIComponent by workspace.dockedComponentProperty

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

    private val pluginEventProperty = SimpleObjectProperty<PluginType?>()

    init {
        subscribe<PluginOpenedEvent> {
            pluginEventProperty.set(it.type)

            when(it.type) {
                PluginType.RECORDER -> breadCrumbsBar.addItem(recorderBreadCrumb)
                PluginType.EDITOR -> breadCrumbsBar.addItem(editorBreadCrumb)
                PluginType.MARKER -> breadCrumbsBar.addItem(markerBreadCrumb)
            }
        }
        subscribe<PluginClosedEvent> {
            pluginEventProperty.set(null)

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

        pluginEventProperty.get()?.let {
            fire(PluginClosedEvent(it))
        }
    }

    fun back() {
        workspace.navigateBack()
    }

    fun forward() {
        workspace.navigateForward()
    }
}
