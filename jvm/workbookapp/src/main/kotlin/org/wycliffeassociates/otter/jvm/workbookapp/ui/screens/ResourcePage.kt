package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.workbookheader.workbookheader
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.styles.ResourceListStyles
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view.ResourceListView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ResourceListViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat
import java.util.concurrent.Callable

class ResourcePage : Fragment() {
    private val workbookDataStore: WorkbookDataStore by inject()
    private val resourceListViewModel: ResourceListViewModel by inject()
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(breadcrumbTitleBinding())
        iconProperty.set(FontIcon(MaterialDesign.MDI_BOOKMARK))
        onClickAction {
            navigator.dock(this@ResourcePage)
        }
    }

    init {
        importStylesheet<ResourceListStyles>()
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

    private fun breadcrumbTitleBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                when {
                    workbookDataStore.activeChunkProperty.value != null ->
                        workbookDataStore.activeChunkProperty.value.let { chunk ->
                            MessageFormat.format(
                                messages["chunkTitle"],
                                messages["chunk"],
                                chunk.start
                            )
                        }
                    navigator.workspace.dockedComponentProperty.value == this -> messages["chunk"]
                    else -> messages["chapter"]
                }
            },
            navigator.workspace.dockedComponentProperty,
            workbookDataStore.activeChunkProperty
        )
    }
}
