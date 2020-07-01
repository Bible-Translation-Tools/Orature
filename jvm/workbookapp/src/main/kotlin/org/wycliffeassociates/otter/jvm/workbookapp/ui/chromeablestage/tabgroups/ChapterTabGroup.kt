package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import javafx.scene.control.Tab
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.jvm.workbookapp.ui.cardgrid.view.CardGridFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class ChapterTabGroup : TabGroup() {
    private val workbookViewModel: WorkbookViewModel by inject()
    private val tabMap: MutableMap<String, Tab> = mutableMapOf()

    override fun activate() {
        workbookViewModel.activeChapterProperty.set(null)
        val activeResourceMetadata = workbookViewModel.activeResourceMetadataProperty.value

        createTabs()
        tabPane.tabs.addAll(tabMap.values)

        // Adding these tabs can change the active resource property so we need to
        // change it back to what it was originally
        if (shouldRestoreActiveResourceMetadata(activeResourceMetadata)) {
            restoreActiveResourceMetadata(activeResourceMetadata)
        }
    }

    override fun deactivate() {
        tabMap.clear()
    }

    private fun shouldRestoreActiveResourceMetadata(metadataToRestore: ResourceMetadata?): Boolean {
        return metadataToRestore != null && getAssociatedMetadatas().contains(metadataToRestore)
    }

    private fun getTargetBookResourceMetadata(): ResourceMetadata {
        return workbookViewModel.workbook.target.resourceMetadata
    }

    private fun getAssociatedMetadatas(): Sequence<ResourceMetadata> {
        return sequenceOf(getTargetBookResourceMetadata()) + workbookViewModel.workbook.target.linkedResources
    }

    private fun createTabs() {
        getAssociatedMetadatas().forEach { metadata ->
            tabMap.putIfAbsent(metadata.identifier, ChapterSelectTab(metadata))
        }
    }

    private fun restoreActiveResourceMetadata(resourceMetadata: ResourceMetadata) {
        workbookViewModel.activeResourceMetadataProperty.set(resourceMetadata)
        tabMap[resourceMetadata.identifier]?.select()
    }

    private inner class ChapterSelectTab(val resourceMetadata: ResourceMetadata) : Tab() {
        init {
            text = resourceMetadata.identifier
            add(CardGridFragment().root)
            onSelected {
                workbookViewModel.activeResourceMetadataProperty.set(resourceMetadata)
                workbookViewModel.setProjectAudioDirectory(resourceMetadata)
            }
        }

        private fun onSelected(op: () -> Unit) {
            selectedProperty().onChange { selected ->
                if (selected) {
                    op()
                }
            }
        }
    }
}
