package org.wycliffeassociates.otter.jvm.controls.popup

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.model.NotificationStatusType
import tornadofx.*
import tornadofx.FX.Companion.messages

class NotificationSnackBar: HBox() {

    val titleProperty = SimpleStringProperty()
    val messageProperty = SimpleStringProperty()
    val statusTypeProperty = SimpleObjectProperty<NotificationStatusType>()
    val actionIconProperty = SimpleObjectProperty<Ikon>()
    val actionTextProperty = SimpleStringProperty()

    private val mainActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>(null)
    private val dismissActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("wa-snack-bar")

        button {
            addClass("btn", "btn--icon", "btn--borderless", "success-btn-icon")
            toggleClass("success-btn-icon", statusTypeProperty.value == NotificationStatusType.SUCCESSFUL)
            toggleClass("danger-btn-icon", statusTypeProperty.value == NotificationStatusType.FAILED)
            graphic = if (statusTypeProperty.value == NotificationStatusType.SUCCESSFUL) {
                FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply {
                    addClass("active-icon")
                }
            } else {
                FontIcon(MaterialDesign.MDI_ALERT).apply {
                    addClass("danger-icon")
                }
            }
            isFocusTraversable = false
            isMouseTransparent = true
        }
        vbox {
            addClass("wa-snack-bar__labels")
            label(titleProperty) {
                addClass("h4", "notification-title")
                toggleClass("successful-text", statusTypeProperty.value == NotificationStatusType.SUCCESSFUL)
                toggleClass("danger-text", statusTypeProperty.value == NotificationStatusType.FAILED)
            }
            label(messageProperty) {
                addClass("h5", "notification-subtitle")
            }
        }
        region { hgrow = Priority.ALWAYS }
        button {
            addClass("btn", "btn--secondary")
            textProperty().bind(actionTextProperty)
            tooltipProperty().bind(actionTextProperty.objectBinding { Tooltip(it) })
            graphicProperty().bind(actionIconProperty.objectBinding { it ->
                it?.let { FontIcon(it) }
            })
            onActionProperty().bind(mainActionProperty)
            visibleWhen { actionTextProperty.isNotNull }
            managedWhen(visibleProperty())
        }

        button {
            addClass("btn", "btn--icon", "btn--borderless")
            tooltip(messages["dismiss"])
            graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE).apply {
                addClass("dismiss-icon")
            }
            onActionProperty().bind(dismissActionProperty)
        }
    }

    fun setOnMainAction(op: () -> Unit) {
        mainActionProperty.set(EventHandler { op() })
    }

    fun setOnDismiss(op: () -> Unit) {
        dismissActionProperty.set(EventHandler { op() })
    }
}