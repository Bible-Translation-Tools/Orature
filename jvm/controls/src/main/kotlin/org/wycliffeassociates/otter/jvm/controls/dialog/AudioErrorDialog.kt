/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import java.util.concurrent.Callable
import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class AudioErrorDialog : OtterDialog() {
    val titleTextProperty = SimpleStringProperty()
    val messageTitleTextProperty = SimpleStringProperty()
    val messageTextProperty = SimpleStringProperty()
    val cancelButtonTextProperty = SimpleStringProperty()
    val backgroundImageProperty = SimpleObjectProperty<Image>()

    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onCancelActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val content = vbox {
        addClass("audio-error-dialog")
        hbox {
            addClass("audio-error-dialog__title-bar")
            label(titleTextProperty).apply {
                addClass("audio-error-dialog__title")
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("add-plugin-dialog__btn--close")
                graphic = FontIcon("gmi-close")
                onActionProperty().bind(onCancelActionProperty())
            }
        }
        stackpane {
            addClass("audio-error-dialog__header")
            vgrow = Priority.ALWAYS

            hbox {
                backgroundProperty().bind(backgroundBinding())
            }
        }
        hbox {
            addClass("audio-error-dialog__body")
            vgrow = Priority.ALWAYS

            vbox {
                label(messageTitleTextProperty) {
                    addClass("audio-error-dialog__subtitle")
                }

                label(messageTextProperty) {
                    addClass("audio-error-dialog__message")
                }
            }
        }
        hbox {
            addClass("audio-error-dialog__footer")

            button(cancelButtonTextProperty) {
                addClass("btn", "btn--primary")
                graphic = FontIcon("gmi-close")
                onActionProperty().bind(onCancelActionProperty())
                visibleProperty().bind(onCancelActionProperty.isNotNull)
                managedProperty().bind(visibleProperty())
            }

            region {
                addClass("audio-error-dialog__footer-spacer")
                hgrow = Priority.ALWAYS
                managedProperty().bind(onCancelActionProperty.isNotNull)
            }

            visibleProperty().bind(
                onCloseActionProperty.isNotNull
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
                backgroundImageProperty.value?.let {
                    background = Background(backgroundImage(it))
                }
                background
            },
            backgroundImageProperty
        )
    }

    private fun backgroundImage(image: Image): BackgroundImage {
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
}

fun audioerrordialog(setup: AudioErrorDialog.() -> Unit = {}): AudioErrorDialog {
    val audioErrorDialog = AudioErrorDialog()
    audioErrorDialog.setup()
    return audioErrorDialog
}
