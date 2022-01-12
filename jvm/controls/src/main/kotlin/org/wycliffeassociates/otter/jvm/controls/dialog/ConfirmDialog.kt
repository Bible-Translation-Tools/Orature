/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.controls.dialog

import com.jfoenix.controls.JFXProgressBar
import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.effect.Blend
import javafx.scene.effect.BlendMode
import javafx.scene.effect.ColorAdjust
import javafx.scene.effect.ColorInput
import javafx.scene.effect.Effect
import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*
import java.io.File
import java.util.concurrent.Callable

class ConfirmDialog : OtterDialog() {

    val titleTextProperty = SimpleStringProperty()
    val messageTextProperty = SimpleStringProperty()
    val backgroundImageFileProperty = SimpleObjectProperty<File>()
    val confirmButtonTextProperty = SimpleStringProperty()
    val cancelButtonTextProperty = SimpleStringProperty()
    val progressTitleProperty = SimpleStringProperty()
    val showProgressBarProperty = SimpleBooleanProperty()

    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onCancelActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onConfirmActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val content = vbox {
        addClass("confirm-dialog")

        stackpane {
            addClass("confirm-dialog__header")
            vgrow = Priority.ALWAYS

            hbox {
                effectProperty().bind(
                    backgroundEffectBinding(
                        this.widthProperty(),
                        this.heightProperty()
                    )
                )
                backgroundProperty().bind(backgroundBinding())
            }

            hbox {
                addClass("confirm-dialog__title-bar")

                label(titleTextProperty).apply {
                    addClass("confirm-dialog__title")
                }
                region {
                    hgrow = Priority.ALWAYS
                }
                button {
                    addClass("btn", "btn--secondary", "confirm-dialog__btn--close")
                    tooltip(messages["close"])
                    graphic = FontIcon("gmi-close")
                    onActionProperty().bind(onCloseActionProperty())
                    visibleProperty().bind(onCloseActionProperty().isNotNull)
                }
            }
        }
        hbox {
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS

            label(messageTextProperty) {
                addClass("confirm-dialog__message")
            }
        }
        vbox {
            addClass("confirm-dialog__progress")
            add(
                JFXProgressBar().apply {
                    prefWidthProperty().bind(this@vbox.widthProperty())
                }
            )
            label(progressTitleProperty).apply {
                addClass("confirm-dialog__progress-title")
            }
            visibleProperty().bind(showProgressBarProperty)
            managedProperty().bind(visibleProperty())
        }
        hbox {
            addClass("confirm-dialog__footer")

            button(cancelButtonTextProperty) {
                addClass("btn", "btn--primary")
                tooltip { textProperty().bind(this@button.textProperty()) }
                graphic = FontIcon("gmi-close")
                onActionProperty().bind(onCancelActionProperty())
                visibleProperty().bind(onCancelActionProperty.isNotNull)
                managedProperty().bind(visibleProperty())
            }

            region {
                addClass("confirm-dialog__footer-spacer")
                hgrow = Priority.ALWAYS
                managedProperty().bind(onCancelActionProperty.isNotNull)
            }

            button(confirmButtonTextProperty) {
                addClass("btn", "btn--secondary", "btn--borderless")
                tooltip { textProperty().bind(this@button.textProperty()) }
                graphic = FontIcon("gmi-remove")
                onActionProperty().bind(onConfirmActionProperty())
                visibleProperty().bind(onConfirmActionProperty.isNotNull)
                managedProperty().bind(visibleProperty())
            }

            visibleProperty().bind(
                onCancelActionProperty.isNotNull.or(onConfirmActionProperty.isNotNull)
            )
            managedProperty().bind(visibleProperty())
        }
    }

    init {
        setContent(content)
    }

    private fun backgroundBinding(): ObjectBinding<Background?> {
        return Bindings.createObjectBinding(
            Callable {
                var background: Background? = null
                backgroundImageFileProperty.value?.let {
                    background = Background(backgroundImage(it))
                }
                background
            },
            backgroundImageFileProperty
        )
    }

    private fun backgroundEffectBinding(
        widthProperty: ReadOnlyDoubleProperty,
        heightProperty: ReadOnlyDoubleProperty
    ): ObjectBinding<Effect> {
        return Bindings.createObjectBinding(
            Callable {
                val colorAdjust = ColorAdjust().apply {
                    saturation = -1.0
                }

                val colorInput = ColorInput().apply {
                    x = 0.0
                    y = 0.0
                    widthProperty().bind(widthProperty)
                    heightProperty().bind(heightProperty)
                    paint = Color.valueOf("#1067c4")
                }

                val blend = Blend(
                    BlendMode.MULTIPLY,
                    colorAdjust,
                    colorInput
                )

                blend as Effect
            },
            widthProperty,
            heightProperty
        )
    }

    private fun backgroundImage(file: File): BackgroundImage {
        val image = Image(file.inputStream())
        val backgroundSize = BackgroundSize(
            1.0,
            1.0,
            true,
            true,
            true,
            false
        )
        return BackgroundImage(
            image,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            backgroundSize
        )
    }

    fun onCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op.invoke() })
    }

    fun onCloseActionProperty(): ObjectProperty<EventHandler<ActionEvent>> {
        return onCloseActionProperty
    }

    fun onCancelAction(op: () -> Unit) {
        onCancelActionProperty.set(EventHandler { op.invoke() })
    }

    fun onCancelActionProperty(): ObjectProperty<EventHandler<ActionEvent>> {
        return onCancelActionProperty
    }

    fun onConfirmAction(op: () -> Unit) {
        onConfirmActionProperty.set(EventHandler { op.invoke() })
    }

    fun onConfirmActionProperty(): ObjectProperty<EventHandler<ActionEvent>> {
        return onConfirmActionProperty
    }
}

fun confirmdialog(setup: ConfirmDialog.() -> Unit = {}): ConfirmDialog {
    val confirmDialog = ConfirmDialog()
    confirmDialog.setup()
    return confirmDialog
}
