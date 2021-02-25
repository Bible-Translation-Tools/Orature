package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.CardGridFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ResourceListFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookViewModel
import tornadofx.*

class RecordableTabGroup : TabGroup() {
    private val workbookViewModel: WorkbookViewModel by inject()

    override fun activate() {
        workbookViewModel.activeChunkProperty.set(null)
        workbookViewModel.activeResourceComponentProperty.set(null)
        workbookViewModel.activeResourceProperty.set(null)

        when (workbookViewModel.activeResourceMetadata.type) {
            ContainerType.Book -> createChunkTab()
            ContainerType.Help -> createResourceTab()
            else -> Unit
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
