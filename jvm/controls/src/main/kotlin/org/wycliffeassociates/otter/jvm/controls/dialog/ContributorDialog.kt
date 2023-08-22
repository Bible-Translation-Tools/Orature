package org.wycliffeassociates.otter.jvm.controls.dialog

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.jvm.controls.ContributorInfo
import org.wycliffeassociates.otter.jvm.controls.model.ContributorCellData
import tornadofx.*

class ContributorDialog : OtterDialog() {
    val contributors = observableListOf<Contributor>()

    val saveContributorCallback = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val content = VBox().apply {
        addClass("contributor-dialog")

        vbox {
            spacing = 10.0
            hbox {
                addClass("contributor-dialog__header")
                label (messages["modifyContributors"]) {
                    addClass("h3")
                }
                region { hgrow = Priority.ALWAYS }
                button {
                    addClass("btn", "btn--secondary")
                    graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
                    tooltip(messages["close"])
                    action { close() }
                }
            }
            add(createContributorSection())
        }

        textflow {
            text(messages["exportLicenseDescription"]) {
                addClass("contributor__section-text", "contributor__license-text")
            }
            hyperlink(messages["licenseCCBYSA"]) {
                addClass("wa-text--hyperlink", "contributor__license-link")
                val url = "https://creativecommons.org/licenses/by-sa/4.0/"
                tooltip(url)
                action {
                    FX.application.hostServices.showDocument(url)
                }
            }
        }

        hbox {
            hgrow = Priority.ALWAYS
            addClass("contributor-dialog__action")

            button (messages["save"]) {
                addClass("btn", "btn--primary", "btn--borderless", "contributor-dialog__action__confirm-btn")
                graphic = FontIcon(MaterialDesign.MDI_CHECK_CIRCLE)
                tooltip(this.text)
                hgrow = Priority.ALWAYS

                action {
                    saveContributorCallback.value?.handle(ActionEvent())
                    close()
                }
            }
        }
    }

    init {
        setContent(content)
    }

    private fun createContributorSection(): ContributorInfo {
        return ContributorInfo(contributors)
            .apply {
                addContributorCallbackProperty.set(
                    EventHandler {
                        contributors.add(Contributor(it.source as String))
                    }
                )
                removeContributorCallbackProperty.set(
                    EventHandler {
                        val indexToRemove = it.source as Int
                        contributors.removeAt(indexToRemove)
                    }
                )
                editContributorCallbackProperty.set(
                    EventHandler {
                        val data = it.source as ContributorCellData
                        contributors[data.index] = Contributor(data.name)
                        lastModifiedIndex.set(data.index)
                    }
                )
            }
    }
}