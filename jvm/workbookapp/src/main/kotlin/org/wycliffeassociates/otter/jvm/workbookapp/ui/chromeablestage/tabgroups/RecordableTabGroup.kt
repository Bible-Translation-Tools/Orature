package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.common.data.model.ContainerType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.cardgrid.view.CardGridFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resources.view.ResourceListFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class RecordableTabGroup : TabGroup() {
    private val workbookViewModel: WorkbookViewModel by inject()

    override fun activate() {
        workbookViewModel.activeChunkProperty.set(null)

        when (workbookViewModel.activeResourceMetadata.type) {
            ContainerType.Book.slug -> createChunkTab()
            ContainerType.Help.slug -> createResourceTab()
        }
    }

    private fun createChunkTab() {
        tabPane.apply {
            tab<CardGridFragment>()
        }
    }

    private fun createResourceTab() {
        tabPane.apply {
            tab<ResourceListFragment>()
        }
    }
}
