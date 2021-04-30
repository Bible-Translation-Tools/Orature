package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import org.wycliffeassociates.otter.jvm.markerapp.app.view.MarkerView

class ChunkPage: MarkerView() {

    val chunkvm: ChunkingViewModel by inject()

    override fun onDock() {
        super.onDock()
        chunkvm.titleProperty.set("Chunking")
        chunkvm.stepProperty.set("Add chunk markers to the source audio. Try to mark complete thoughts.")
    }
}
