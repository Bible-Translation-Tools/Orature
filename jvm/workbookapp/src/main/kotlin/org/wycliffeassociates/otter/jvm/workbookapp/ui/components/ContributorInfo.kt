package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import tornadofx.*
import tornadofx.FX.Companion.messages

class ContributorInfo(
    private val contributors: ObservableList<Contributor>
) : VBox() {
    var contributorField: TextField by singleAssign()

    val removeContributorCallbackProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val addContributorCallbackProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("contributor__container")

        vbox {
            label(messages["contributorHeading"]) {
                addClass("contributor__section-title")
            }
            text(messages["contributorDescription"]) {
                addClass("contributor__section-text")
            }
        }
        hbox {
            spacing = 20.0
            alignment = Pos.CENTER

            textfield {
                contributorField = this
                hgrow = Priority.ALWAYS

                addClass("txt-input", "contributor__text-input")
                promptText = messages["contributorName"]
            }.hide()
            button(messages["addContributor"]) {
                useMaxWidth = true
                hgrow = Priority.SOMETIMES

                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_PLUS)
                setOnAction {
                    if (!contributorField.isVisible) {
                        contributorField.show()
                    } else {
                        addContributorCallbackProperty.value?.handle(
                            ActionEvent(contributorField.text, null)
                        )
                        contributorField.clear()
                    }
                }
            }
        }
        vbox {
            vgrow = Priority.ALWAYS
            addClass("contributor__input-group")

            listview(contributors) {
                addClass("wa-list-view", "contributor__list")
                vgrow = Priority.ALWAYS

                setCellFactory {
                    ContributorListCell().apply {
                        onRemoveContributorActionProperty.bind(removeContributorCallbackProperty)
                    }
                }
            }
        }
    }
}