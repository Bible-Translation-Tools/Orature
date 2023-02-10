package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Verse
import tornadofx.*

class Narration(verses: ObservableList<Verse>? = null) : VBox() {
    private val currentVerseLabelProperty = SimpleStringProperty()
    private val onCurrentVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        stackpane {
            addClass("narration__recording")
            alignment = Pos.CENTER

            hbox {
                listview(verses) {
                    hgrow = Priority.ALWAYS

                    addClass("wa-list-view")

                    setCellFactory {
                        NarrationRecordCell().apply {
                            setOnPlay {
                                println("Playing verse ${item.label}")
                            }

                            setOnOpenApp {
                                println("Opening verse ${item.label} in external app...")
                            }

                            setOnRecordAgain {
                                println("Recording verse ${item.label} again")
                            }
                        }
                    }
                    orientation = Orientation.HORIZONTAL
                }
                stackpane {
                    addClass("narration__volume-bar")

                    vbox {
                        addClass("narration__volume-bar__value")

                        maxHeight = 50.0
                    }
                }
            }

            vbox {
                addClass("narration__recording-tip")
                alignment = Pos.CENTER_LEFT

                label("Tip") {
                    addClass("narration__recording-tip-title")
                    style = "-fx-font-weight: bold;"
                }
                label("Press the down key on your keyboard to navigate to the next verse.")

                isVisible = false
            }
        }
        stackpane {
            addClass("narration__verses")

            narrationlistview(verses) {
                addClass("narration__list")

                currentVerseLabelProperty.bind(selectedVerseLabelProperty)
                onCurrentVerseActionProperty.bind(onSelectedVerseActionProperty)

                setOnRecord {
                    println("Recording verse ${it.label}")
                }
            }

            vbox {
                addClass("narration__selected-verse")

                hbox {
                    addClass("narration__selected-verse-controls")

                    label {
                        textProperty().bind(currentVerseLabelProperty.stringBinding {
                            "Current: Verse $it"
                        })
                    }
                    region {
                        hgrow = Priority.ALWAYS
                    }
                    button("Resume") {
                        addClass("btn", "btn--primary")
                        graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                        onActionProperty().bind(onCurrentVerseActionProperty)
                    }
                }

                visibleProperty().bind(currentVerseLabelProperty.isNotNull)
                managedProperty().bind(visibleProperty())
            }
        }
    }
}