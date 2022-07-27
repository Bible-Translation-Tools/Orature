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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.scene.control.TabPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.utils.enableContentAnimation
import org.wycliffeassociates.otter.jvm.utils.getNotNull
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithListener
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class RecordResourcePage : View() {
    private val viewModel: RecordResourceViewModel by inject()
    private val navigator: NavigationMediator by inject()
    private val workbookDataStore: WorkbookDataStore by inject()

    val tabPane = TabPane().apply {
        addClass("wa-tab-pane")
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        enableContentAnimation()
    }

    override val root = tabPane

    private val tabs: List<RecordableTab> = listOf(
        recordableTab(ContentType.TITLE),
        recordableTab(ContentType.BODY)
    )

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            workbookDataStore.activeChunkProperty.stringBinding { chunk ->
                chunk?.let {
                    MessageFormat.format(
                        messages["chunkTitle"],
                        messages["chunk"],
                        chunk.start
                    )
                } ?: messages["chapter"]
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE))
        setOnAction {
            fire(NavigationRequestEvent(this@RecordResourcePage))
        }
    }

    private fun recordableTab(contentType: ContentType): RecordableTab {
        return RecordableTab(
            viewModel = viewModel.contentTypeToViewModelMap.getNotNull(contentType),
            onTabSelect = viewModel::onTabSelect
        ).apply {
            whenSelected {
                shortcut(Shortcut.RECORD.value, ::recordNewTake)
            }
        }
    }

    private fun addRemoveTabs(tab: RecordableTab, rec: Recordable?) {
        rec?.let {
            if (!tabPane.tabs.contains(tab)) tabPane.tabs.add(tab)
        } ?: tabPane.tabs.remove(tab)
    }

    override fun onDock() {
        tabs.forEach { recordableTab ->
            recordableTab.bindProperties()
            recordableTab.recordableProperty.onChangeAndDoNowWithListener {
                addRemoveTabs(recordableTab, it)
            }.let { recordableTab.recordableListener = it }
        }
        navigator.dock(this, breadCrumb)
    }

    override fun onUndock() {
        tabs.forEach { recordableTab ->
            recordableTab.unbindProperties()
            recordableTab.removeListeners()
        }
        tabPane.tabs.clear()
    }
}
