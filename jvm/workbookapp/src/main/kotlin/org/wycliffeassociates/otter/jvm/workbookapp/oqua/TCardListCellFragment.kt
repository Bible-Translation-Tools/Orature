package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import tornadofx.*

class TCardListCellFragment: ListCellFragment<TranslationCard>() {
    private val sourceProperty = stringBinding(itemProperty) {
        value?.translation?.source?.name
    }
    private val targetProperty = stringBinding(itemProperty) {
        value?.translation?.target?.name
    }
    private val hasQuestionsProperty = booleanBinding(itemProperty) {
        value?.hasQuestions ?: false
    }
    private val questionsURLProperty = stringBinding(itemProperty) {
        "https://content.bibletranslationtools.org/WA-Catalog/${value?.translation?.source?.slug}_tq"
    }
    private val projects = objectBinding(itemProperty) {
        value?.projects
    }

    override val root = vbox {
        hbox {
            text(sourceProperty)
            text(" -> ")
            text(targetProperty)
        }

        vbox {
            style { padding = box(20.0.px) }
            hiddenWhen(hasQuestionsProperty)
            managedWhen(visibleProperty())
            text("You do not have the questions downloaded for this language")
            hbox {
                text("You can find them at ")
                text(questionsURLProperty) {
                    style {
                        fontWeight = FontWeight.BOLD
                    }
                }
            }
            text("Download the zip file and import it using the import button of Orature")
        }

        listview<Workbook> {
            visibleWhen(hasQuestionsProperty)
            managedWhen(visibleProperty())
            itemsProperty().bind(projects)
            cellFragment(ProjectListCellFragment::class)
        }
    }
}