/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.workbookheader.workbookheader
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view.ResourceListView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ResourceListViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class ResourcePage : View() {
    private val workbookDataStore: WorkbookDataStore by inject()
    private val resourceListViewModel: ResourceListViewModel by inject()
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            workbookDataStore.activeChapterProperty.stringBinding {
                it?.let {
                    MessageFormat.format(
                        messages["chapterTitle"],
                        messages["chapter"],
                        it.sort
                    )
                } ?: messages["chapter"]
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_FILE))
        onClickAction {
            navigator.dock(this@ResourcePage)
        }
    }

    init {
        importStylesheet(resources.get("/css/resource-page.css"))
    }

    override val root = vbox {
        add(
            workbookheader {
                labelText = MessageFormat.format(
                    messages["chapterResourcesLabel"],
                    messages[workbookDataStore.chapter.label],
                    workbookDataStore.chapter.title,
                    messages["resources"]
                )
                filterText = messages["hideCompleted"]
                workbookProgressProperty.bind(resourceListViewModel.completionProgressProperty)
                resourceListViewModel.isFilterOnProperty.bind(isFilterOnProperty)
            }
        )
        add(
            ResourceListView(
                resourceListViewModel.filteredResourceGroupCardItemList,
                resourceListViewModel.isFilterOnProperty,
                navigator
            ).apply {
                whenDocked {
                    resourceListViewModel.selectedGroupCardItem.get()?.let {
                        scrollTo(it)
                        resourceListViewModel.selectedGroupCardItem.set(null)
                        resourceListViewModel.calculateCompletionProgress()
                    }
                }
            }
        )
    }

    override fun onDock() {
        super.onDock()
        workbookDataStore.activeChunkProperty.set(null)
        workbookDataStore.activeResourceComponentProperty.set(null)
        workbookDataStore.activeResourceProperty.set(null)
        navigator.dock(this, breadCrumb)
    }
}
