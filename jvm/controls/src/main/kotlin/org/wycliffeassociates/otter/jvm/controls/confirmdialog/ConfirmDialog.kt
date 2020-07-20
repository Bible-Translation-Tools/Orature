package org.wycliffeassociates.otter.jvm.controls.confirmdialog

import com.jfoenix.controls.JFXButton
import javafx.application.Platform
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
import javafx.stage.Modality
import javafx.stage.StageStyle
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import java.io.File
import java.util.concurrent.Callable

class ConfirmDialog : Fragment() {

    val titleTextProperty = SimpleStringProperty()
    val messageTextProperty = SimpleStringProperty()
    val backgroundImageFileProperty = SimpleObjectProperty<File>()
    val confirmButtonTextProperty = SimpleStringProperty()
    val cancelButtonTextProperty = SimpleStringProperty()

    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onCancelActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onConfirmActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    val showDialogProperty = SimpleBooleanProperty()

    init {
        importStylesheet(javaClass.getResource("/css/confirm-dialog.css").toExternalForm())

        showDialogProperty.onChangeAndDoNow {
            it?.let {
                Platform.runLater {
                    if (it) open() else close()
                }
            }
        }
    }

    override val root = vbox {
        stackpane {
            addClass("confirm-dialog__header")
            vgrow = Priority.ALWAYS

            prefHeightProperty().bind(this@vbox.heightProperty().divide(2))

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
                add(
                    JFXButton().apply {
                        addClass("btn", "btn--secondary", "confirm-dialog__btn--close")
                        graphic = FontIcon("gmi-close")
                        onActionProperty().bind(onCloseActionProperty())
                    }
                )
            }
        }
        hbox {
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS

            label(messageTextProperty) {
                addClass("confirm-dialog__message")
            }
        }
        hbox {
            addClass("confirm-dialog__footer")

            add(
                JFXButton().apply {
                    addClass("btn", "btn--primary", "confirm-dialog__btn--cancel")
                    graphic = FontIcon("gmi-close")
                    textProperty().bind(cancelButtonTextProperty)
                    onActionProperty().bind(onCancelActionProperty())
                }
            )

            region {
                addClass("confirm-dialog__footer-spacer")
                hgrow = Priority.ALWAYS
            }

            add(
                JFXButton().apply {
                    addClass("btn", "btn--secondary", "confirm-dialog__btn--confirm")
                    graphic = FontIcon("gmi-remove")
                    textProperty().bind(confirmButtonTextProperty)
                    onActionProperty().bind(onConfirmActionProperty())
                }
            )
        }
    }

    fun open() {
        openModal(StageStyle.UNDECORATED, Modality.APPLICATION_MODAL, false)
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
            false,
            true
        )
        return BackgroundImage(
            image,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.DEFAULT,
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
