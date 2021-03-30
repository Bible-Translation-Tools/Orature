package org.wycliffeassociates.otter.jvm.controls.breadcrumbs

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*
import java.util.concurrent.Callable

class BreadCrumb: HBox() {

    val iconProperty = SimpleObjectProperty<FontIcon>()
    val titleProperty = SimpleStringProperty()
    val activeTitleProperty = SimpleStringProperty()
    val isActiveProperty = SimpleBooleanProperty(false)
    val tooltipTextProperty = SimpleStringProperty()
    val onClickProperty = SimpleObjectProperty<EventHandler<MouseEvent>>()

    init {
        styleClass.setAll("breadcrumb")

        label {
            graphicProperty().bind(iconProperty)
            textProperty().bind(titleBinding())

            addClass("breadcrumb__content")

            isActiveProperty.onChange {
                if (it) {
                    addClass("breadcrumb--active")
                } else {
                    removeClass("breadcrumb--active")
                }
            }

            onMouseClickedProperty().bind(onClickProperty)
        }

        label {
            addClass("breadcrumb__separator")

            graphic = FontIcon("mdi-play")
            hiddenWhen(isActiveProperty)
            managedWhen(visibleProperty())
        }

        label {
            addClass("breadcrumb__help")

            graphic = FontIcon("mdi-help-circle")
            visibleWhen(
                Bindings.and(isActiveProperty, tooltipTextProperty.isNotEmpty)
            )
            managedWhen(visibleProperty())

            tooltip {
                textProperty().bind(tooltipTextProperty)
                prefWidth = 256.0
            }
        }
    }

    fun titleBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                if (isActiveProperty.value) {
                    activeTitleProperty.value
                } else {
                    titleProperty.value
                }
            },
            titleProperty,
            activeTitleProperty,
            isActiveProperty
        )
    }

    fun onClickAction(op: () -> Unit) {
        onClickProperty.set(EventHandler { op() })
    }
}
