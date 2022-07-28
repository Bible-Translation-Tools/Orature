package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.binding.Bindings
import javafx.scene.control.ToggleGroup
import tornadofx.*

class TQListCellFragment: ListCellFragment<Question>() {
    private val toggleGroup = ToggleGroup()

    private val questionProperty = Bindings.createStringBinding(
        { itemProperty.value?.question },
        itemProperty
    )
    private val answerProperty = Bindings.createStringBinding(
        { itemProperty.value?.answer },
        itemProperty
    )
    private val verseProperty = Bindings.createStringBinding(
        {
            itemProperty.value?.let { question ->
                getVerseLabel(question)
            }
        },
        itemProperty
    )

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