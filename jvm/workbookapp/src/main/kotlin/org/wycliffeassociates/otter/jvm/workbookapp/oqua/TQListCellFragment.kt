package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.scene.control.ToggleGroup
import tornadofx.*

class TQListCellFragment: ListCellFragment<Question>() {
    private val toggleGroup = ToggleGroup()

    private val questionProperty = stringBinding(itemProperty) {
        value?.question
    }
    private val answerProperty = stringBinding(itemProperty) {
        value?.answer
    }
    private val verseProperty = stringBinding(itemProperty) {
        value?.let { question ->
            getVerseLabel(question)
        }
    }

    private fun getVerseLabel(question: Question): String {
        return if (question.start == question.end) {
            "Verse ${question.start}"
        } else {
            "Verses ${question.start} - ${question.end}"
        }
    }

    override val root = vbox {
        text(verseProperty)
        text(questionProperty)

        vbox {
            managedWhen(visibleProperty()) // Included for the possibility of hidding the answers initially

            text(answerProperty)
            hbox {
                val correct = togglebutton("Correct", toggleGroup) {
                    action {
                        item.result = "correct"
                    }
                }
                val incorrect = togglebutton("Incorrect", toggleGroup) {
                    action {
                        item.result = "incorrect"
                    }
                }
                val invalid = togglebutton("Invalid", toggleGroup) {
                    action {
                        item.result = "invalid"
                    }
                }

                itemProperty.onChange {
                    when (it?.result) {
                        "correct" -> toggleGroup.selectToggle(correct)
                        "incorrect" -> toggleGroup.selectToggle(incorrect)
                        "invalid" -> toggleGroup.selectToggle(invalid)
                        else -> toggleGroup.selectToggle(null)
                    }
                }
            }
        }
    }
}