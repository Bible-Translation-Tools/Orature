package app.ui

import app.ui.ChartCSS.Companion.progressBarStyle
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*
import java.awt.Button
import kotlin.concurrent.thread

/**
 * sweet code for a heckin cool loading bar
 */
class ProgressBar : View() {
    override val root = vbox {
        var isItDone = false

        style {
            alignment = Pos.TOP_CENTER
        }

        progressbar {
            addClass(progressBarStyle)
            thread {
                while (!isItDone) {
                    for (i in 1..100) {
                        Platform.runLater { progress = i.toDouble() / 100.0 }
                        Thread.sleep(10)
                    }
                }
            }
        }
        button("Stop the progress") {
            action {
                isItDone = true
                println(isItDone)
            }
        }
    }
}

/**
 * garbage code for making buttons/chips of each language selected
 */
class languageSelector : Fragment() {
    var languageList: ArrayList<String> = ArrayList()

    override val root = vbox {
        var language = SimpleStringProperty()

        textfield(language)
        button("Submit") {
            action {
                makeButton(language.value)
            }
        }
        var buttonsHere = hbox {
            // add the buttons to here somehow??
        }
    }

    fun makeButton(language: String) {
        // how do i add properties to the button?
        var button = button(language) {
            action {
                println("you pressed it! the " + language + " button!")
            }
        }
        languageList.add(language) // adding the language to a list works
        add(button) // how do I get this to be added to a different location?
        println(languageList)
    }
}