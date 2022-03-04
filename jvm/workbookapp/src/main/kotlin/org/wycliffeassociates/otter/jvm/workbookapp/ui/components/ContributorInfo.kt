package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.event.EventTarget
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import tornadofx.*
import tornadofx.FX.Companion.messages

class ContributorInfo : VBox() {
    private val contributors = observableListOf(Contributor("Tony T."), Contributor("Jonathan T."))
    var contributorField: TextField by singleAssign()

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
        vbox {
            vgrow = Priority.ALWAYS

            hbox {
                addClass("contributor__input-group")
                contributorField = textfield {
                    addClass("txt-input", "contributor__text-input")
                    promptText = messages["contributorName"]
                }
                button(messages["addContributor"]) {
                    addClass("btn--secondary","btn--borderless")
                    graphic = FontIcon(MaterialDesign.MDI_PLUS)
                    setOnAction {
                        if (contributorField.isVisible) contributorField.hide() else contributorField.show()
                    }
                }
            }
            listview(contributors) {
                addClass("wa-list-view", "contributor__list")
                vgrow = Priority.ALWAYS

                setCellFactory {
                    ContributorListCell()
                }
            }
            button (messages["saveContributors"]) {
                addClass("btn--primary","btn--borderless")
                setOnAction {

                }
            }
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