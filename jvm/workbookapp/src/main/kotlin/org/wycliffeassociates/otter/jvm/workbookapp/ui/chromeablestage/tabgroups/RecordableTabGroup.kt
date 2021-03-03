package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.CardGridFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ResourceListFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class RecordableTabGroup : TabGroup() {
    private val workbookDataStore: WorkbookDataStore by inject()

    override fun activate() {
        workbookDataStore.activeChunkProperty.set(null)
        workbookDataStore.activeResourceComponentProperty.set(null)
        workbookDataStore.activeResourceProperty.set(null)

        when (workbookDataStore.activeResourceMetadata.type) {
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
