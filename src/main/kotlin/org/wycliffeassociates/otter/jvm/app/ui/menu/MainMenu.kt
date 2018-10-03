package org.wycliffeassociates.otter.jvm.app.ui.menu

import javafx.scene.control.MenuBar
import org.wycliffeassociates.otter.common.domain.ImportResourceContainer
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import tornadofx.*


class MainMenu : MenuBar() {

    val languageRepo = Injector.languageRepo
    val metadataRepo = Injector.metadataRepo
    val collectionRepo = Injector.collectionRepo
    val directoryProvider = Injector.directoryProvider

    init {
        with(this) {
            menu("File") {
                item("Import Resource Container") {
                    action {
                        val file = chooseDirectory("Please Select Resource Container to Import")
                        file?.let {
                            val importer = ImportResourceContainer(languageRepo, metadataRepo, collectionRepo, directoryProvider)
                            importer.import(file)
                        }
                    }
                }
            }
        }
    }
}