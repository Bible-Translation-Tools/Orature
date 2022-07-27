/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadcrumbBar
import org.wycliffeassociates.otter.jvm.controls.event.AppCloseRequestEvent
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginCloseFinishedEvent
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginCloseRequestEvent
import tornadofx.*
import java.text.MessageFormat

class NavigationMediator : Component(), ScopedInstance {

    val workbookDataStore: WorkbookDataStore by inject()
    val breadCrumbsBar = BreadcrumbBar()
    private val pluginOpenedProperty = SimpleBooleanProperty(false)
    private var appExitRequested = false

    private val recorderBreadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            workbookDataStore.activeTakeNumberProperty.stringBinding { take ->
                MessageFormat.format(
                    messages["takeTitle"],
                    messages["take"],
                    take
                )
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_LIBRARY_MUSIC))
    }

    private val editorBreadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            workbookDataStore.activeTakeNumberProperty.stringBinding { take ->
                MessageFormat.format(
                    messages["takeTitle"],
                    messages["take"],
                    take
                )
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_LIBRARY_MUSIC))
    }

    private val markerBreadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["addMarkers"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_BOOKMARK_PLUS_OUTLINE))
    }

    init {
        subscribe<PluginOpenedEvent> {
            when (it.type) {
                PluginType.RECORDER -> breadCrumbsBar.addItem(recorderBreadCrumb)
                PluginType.EDITOR -> breadCrumbsBar.addItem(editorBreadCrumb)
                PluginType.MARKER -> breadCrumbsBar.addItem(markerBreadCrumb)
            }
            pluginOpenedProperty.set(true)
        }
        subscribe<PluginClosedEvent> {
            when (it.type) {
                PluginType.RECORDER -> breadCrumbsBar.removeItem(recorderBreadCrumb)
                PluginType.EDITOR -> breadCrumbsBar.removeItem(editorBreadCrumb)
                PluginType.MARKER -> breadCrumbsBar.removeItem(markerBreadCrumb)
            }
            pluginOpenedProperty.set(false)
        }
        subscribe<NavigationRequestEvent> {
            if (pluginOpenedProperty.value) {
                fire(PluginCloseRequestEvent)
            } else {
                dock(it.view)
            }
        }
        subscribe<PluginCloseFinishedEvent> {
            back()
            if (appExitRequested) {
                runLater {
                    Platform.exit()
                    System.exit(0);
                }
            }
        }
        subscribe<AppCloseRequestEvent> {
            appExitRequested = true
            fire(PluginCloseRequestEvent)
        }
    }

    inline fun <reified T : UIComponent> dock(breadCrumb: BreadCrumb? = null) {
        val view = find<T>()
        dock(view, breadCrumb)
    }

    fun dock(view: UIComponent, breadCrumb: BreadCrumb? = null) {
        view.shortcut(Shortcut.GO_BACK.value, ::back)
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
