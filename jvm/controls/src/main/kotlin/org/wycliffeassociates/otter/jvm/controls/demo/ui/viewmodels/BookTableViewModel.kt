package org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels

import tornadofx.*

class BookTableViewModel : ViewModel() {
    val workbookList = observableListOf<WorkbookDemo>(
        WorkbookDemo("John", 0.3, true),
        WorkbookDemo("Acts", 0.0),
        WorkbookDemo("Genesis", 0.1, true),
        WorkbookDemo("Leviticus", 0.5),
        WorkbookDemo("Psalms", 0.8),
        WorkbookDemo("Revelation", 1.0),
        WorkbookDemo("Mark", 0.5),
        WorkbookDemo("Malachi", 1.0),
        WorkbookDemo("Proverbs", 0.2),
        WorkbookDemo("Colossians", 1.0, true)
    )
}

data class WorkbookDemo(val title: String, val progress: Double, val hasSourceAudio: Boolean = false)