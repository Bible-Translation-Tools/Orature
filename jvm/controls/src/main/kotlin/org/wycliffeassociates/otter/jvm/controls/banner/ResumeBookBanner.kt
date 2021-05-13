package org.wycliffeassociates.otter.jvm.controls.banner

import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import org.wycliffeassociates.otter.jvm.controls.skins.banner.ResumeBookBannerSkin
import java.io.File
import java.util.concurrent.Callable

class ResumeBookBanner : Control() {

    val backgroundImageFileProperty = SimpleObjectProperty<File>()
    val bookTitleProperty = SimpleStringProperty()
    val sourceLanguageProperty = SimpleStringProperty()
    val targetLanguageProperty = SimpleStringProperty()
    val resumeTextProperty = SimpleStringProperty("Resume")
    val onResumeActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("resume-book-banner")
    }

    fun onResumeAction(op: () -> Unit) {
        onResumeActionProperty.set(EventHandler { op.invoke() })
    }

    fun backgroundBinding(): ObjectBinding<Background?> {
        return Bindings.createObjectBinding(
            Callable {
                backgroundImageFileProperty.value?.let {
                    Background(backgroundImage(it))
                }
            },
            backgroundImageFileProperty
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
            true
        )
        return BackgroundImage(
            image,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            backgroundSize
        )
    }

    override fun createDefaultSkin(): Skin<*> {
        return ResumeBookBannerSkin(this)
    }
}
