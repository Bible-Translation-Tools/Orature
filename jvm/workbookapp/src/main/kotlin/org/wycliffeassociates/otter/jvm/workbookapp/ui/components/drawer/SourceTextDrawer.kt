package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.NodeOrientation
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.RollingSourceText
import org.wycliffeassociates.otter.jvm.controls.rollingSourceText
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.MessageFormat

class SourceTextDrawer : VBox() {
    val sourceInfoProperty = SimpleStringProperty()
    val textProperty = SimpleStringProperty()
    val licenseProperty = SimpleStringProperty()
    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
    val highlightedChunk = SimpleIntegerProperty(-1)

    private val isCollapsedProperty = SimpleBooleanProperty(false)
    private lateinit var sourceTextContent: RollingSourceText

    init {
        addClass("source-text-drawer")
        hbox {
            addClass("source-text-drawer__header-section")
            label(messages["sourceText"]) {
                addClass("h3", "h3--80")
                visibleWhen { isCollapsedProperty.not() }
                managedWhen(visibleProperty())
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--secondary")
                graphicProperty().bind(isCollapsedProperty.objectBinding {
                    if (it == true) {
                        VBox(
                            FontIcon(MaterialDesign.MDI_CHEVRON_LEFT),
                            FontIcon(MaterialDesign.MDI_BOOK)
                        ).addClass("drawer-graphic-container")
                    } else {
                        HBox(
                            FontIcon(MaterialDesign.MDI_CHEVRON_RIGHT),
                            FontIcon(MaterialDesign.MDI_BOOK)
                        ).addClass("drawer-graphic-container")
                    }
                })
                tooltip {
                    textProperty().bind(isCollapsedProperty.stringBinding {
                        if (it == true) messages["expand"] else messages["collapse"]
                    })
                }
                action {
                    this@SourceTextDrawer.maxWidth = if (isCollapsedProperty.value) {
                        320.0
                    } else {
                        80.0
                    }
                    isCollapsedProperty.set(!isCollapsedProperty.value)
                }
            }
        }
        rollingSourceText {
            sourceTextContent = this
            sourceTitleProperty.bind(sourceInfoProperty)
            sourceTextProperty.bind(textProperty)
            licenseTextProperty.bind(licenseProperty.stringBinding {
                it?.let {
                    MessageFormat.format(messages["licenseStatement"], it)
                } ?: ""
            })
            this.orientationProperty.bind(this@SourceTextDrawer.orientationProperty)

            visibleWhen { isCollapsedProperty.not() }
            managedWhen(visibleProperty())
        }
    }
}