package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.WorkbookInfo
import org.wycliffeassociates.otter.jvm.controls.card.NewTranslationCard2
import org.wycliffeassociates.otter.jvm.controls.card.translationCardWrapper
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookDeleteEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookOpenEvent
import org.wycliffeassociates.otter.jvm.controls.model.TranslationMode
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import java.time.LocalDateTime
import tornadofx.*

class WorkbookTableDemoView : View() {
    val workbookList = observableListOf<WorkbookInfo>(
        WorkbookInfo(0, "John", "", 0.3, LocalDateTime.now(), true),
        WorkbookInfo(0, "Acts", "", 0.0, LocalDateTime.now(), true),
        WorkbookInfo(0, "Genesis", "", 0.1, LocalDateTime.now(), true),
        WorkbookInfo(0, "Leviticus", "", 0.5, LocalDateTime.now(), false),
        WorkbookInfo(0, "Psalms", "", 0.8, LocalDateTime.now(), false),
        WorkbookInfo(0, "Revelation", "", 1.0, LocalDateTime.now(), false),
        WorkbookInfo(0, "Mark", "", 0.5, LocalDateTime.now(), false),
        WorkbookInfo(0, "Malachi", "", 1.0, LocalDateTime.now(), false),
        WorkbookInfo(0, "Proverbs", "", 0.2, LocalDateTime.now(), true),
        WorkbookInfo(0, "Colossians", "", 1.0, LocalDateTime.now(), true),
    )

    init {
        tryImportStylesheet("/css/popup-menu.css")
        tryImportStylesheet("/css/filtered-search-bar.css")
        tryImportStylesheet("/css/table-view.css")
        tryImportStylesheet("/css/translation-card-2.css")

        subscribeToWorkbookEvent()
    }

    private fun subscribeToWorkbookEvent() {
        workspace.subscribe<WorkbookOpenEvent> {
            val targetBook = it.data
            println("open ${targetBook.title}")
        }
        workspace.subscribe<WorkbookExportEvent> {
            val targetBook = it.data
            println("export ${targetBook.title}")
        }
        workspace.subscribe<WorkbookDeleteEvent> {
            val targetBook = it.data
            println("delete ${targetBook.title}")
        }
    }

    override val root = vbox {
        spacing = 10.0
        paddingAll = 20.0
        maxWidth = 300.0

        borderpane {
            center = translationCardWrapper(
                Language("en", "English", "English", "", true, ""),
                Language("fr", "français", "French", "", true, ""),
                TranslationMode.TRANSLATION
            ) {
                top = button("Reset") {
                    action {
                        this@translationCardWrapper.isActiveProperty.set(false)
                    }
                }
            }
        }

        add(
            NewTranslationCard2(
                SimpleObjectProperty<Language>(
                    Language("en", "English", "English", "", true, "")
                ),
                SimpleObjectProperty<Language>(null),
                mode = TranslationMode.NARRATION
            )
        )
    }
}