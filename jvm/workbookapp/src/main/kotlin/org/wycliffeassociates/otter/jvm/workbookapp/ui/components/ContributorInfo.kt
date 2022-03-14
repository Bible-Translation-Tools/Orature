package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import tornadofx.*
import tornadofx.FX.Companion.messages

class ContributorInfo : VBox() {
    private val contributors = observableListOf(Contributor("Tony T."), Contributor("Jonathan T."), Contributor("Joel S."))
    var contributorField: TextField by singleAssign()
    var saveContributorBtn: Button by singleAssign()

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
                        // TODO: add contributor to list
                        contributors.add(Contributor(contributorField.text))
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
                    ContributorListCell()
                }
            }

            button (messages["saveContributors"]) {
                addClass("btn--primary","btn--borderless")
                useMaxWidth = true
                saveContributorBtn = this
                setOnAction {

                }
            }.isVisible = contributors.size != 0
            textflow {
                text(messages["creativeCommonsDescription"]) {
                    addClass("contributor__section-text")
                }
                hyperlink(messages["licenseCCBYSA"]) {
                    addClass("contributor__license-link")
                    action {
                        FX.application.hostServices.showDocument(
                            "https://creativecommons.org/licenses/by-sa/4.0/"
                        )
                    }
                }
                text(messages["creativeCommonsEnd"]) {
                    addClass("contributor__section-text")
                }
            }
        }
    }
}