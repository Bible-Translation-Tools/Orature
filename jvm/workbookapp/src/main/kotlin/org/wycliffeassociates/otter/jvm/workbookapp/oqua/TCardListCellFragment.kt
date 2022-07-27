package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.binding.Bindings
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import tornadofx.*

class TCardListCellFragment: ListCellFragment<TranslationCard>() {
    private val sourceProperty = Bindings.createStringBinding(
        { itemProperty.value?.translation?.source?.name },
        itemProperty
    )
    private val targetProperty = Bindings.createStringBinding(
        { itemProperty.value?.translation?.target?.name },
        itemProperty
    )
    private val hasQuestionsProperty = Bindings.createBooleanBinding(
        { itemProperty.value?.hasQuestions ?: false },
        itemProperty
    )
    private val questionsURLProperty = Bindings.createStringBinding(
        { "https://content.bibletranslationtools.org/WA-Catalog/${itemProperty.value?.translation?.source?.slug}_tq" },
        itemProperty
    )
    private val projects = Bindings.createObjectBinding(
        { itemProperty.value?.projects },
        itemProperty
    )

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