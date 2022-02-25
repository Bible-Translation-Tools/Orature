package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import tornadofx.*
import tornadofx.FX.Companion.messages

class ContributorInfo : VBox() {
    private val contributors = observableListOf(Contributor("Tony T."), Contributor("Jonathan T."))

    init {
        addClass("contributor__container")

        vbox {
            label(messages["licenseHeading"]) {
                addClass("contributor__section-title")
            }
            textflow {
                text(messages["licenseDescription"]) {
                    addClass("contributor__section-text")
                }
                hyperlink("Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)") {
                    addClass("contributor__license-link")
                    action {
                        FX.application.hostServices.showDocument(
                            "https://creativecommons.org/licenses/by-sa/4.0/"
                        )
                    }
                }
            }
        }
        vbox {
            label(messages["contributorHeading"]) {
                addClass("contributor__section-title")
            }
            text(messages["contributorDescription"]) {
                addClass("contributor__section-text")
            }
        }
        vbox {
            hbox {
                addClass("contributor__input-group")
                textfield {
                    addClass("txt-input", "contributor__text-input")
                    promptText = messages["contributorName"]
                }
                button(messages["addContributor"]) {
                    addClass("btn--secondary","btn--borderless")
                    graphic = FontIcon(MaterialDesign.MDI_PLUS)
                    setOnAction {

                    }
                }
            }
            listview(contributors) {
                addClass("wa-list-view", "contributor__list")
                setCellFactory {
                    ContributorListCell()
                }
            }
        }
    }
}

class ContributorListCell : ListCell<Contributor>() {
    private val cellGraphic = ContributorCell()

    override fun updateItem(item: Contributor?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = cellGraphic.apply {
            nameProperty.set(item.name)
            disableProperty().set(true)
        }
    }
}

class ContributorCell : HBox() {
    val nameProperty = SimpleStringProperty()

    init {
        addClass("contributor__list-cell")

        label(nameProperty) {
            addClass("contributor__list-cell__name")
        }
    }
}