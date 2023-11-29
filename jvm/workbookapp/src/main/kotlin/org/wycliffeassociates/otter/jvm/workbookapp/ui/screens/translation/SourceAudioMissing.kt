package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import javafx.stage.FileChooser
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class SourceAudioMissing : View() {
    val viewModel: TranslationViewModel2 by inject()
    val workbookDataStore: WorkbookDataStore by inject()

    override val root = VBox().apply {
        addClass("audio-missing-view")
        vgrow = Priority.ALWAYS
        visibleWhen { viewModel.showAudioMissingViewProperty }
        managedWhen(visibleProperty())

        label {
            addClass("audio-missing__title")
            textProperty().bind(
                workbookDataStore.activeChapterTitleBinding().stringBinding {
                    MessageFormat.format(
                        messages["source_audio_missing_for"],
                        it
                    )
                }
            )
        }
        label(messages["source_audio_missing_description"]) {
            addClass("normal-text", "audio-missing__description")
        }

        vbox {
            addClass("audio-missing__drag-drop-area")

            label {
                graphic = FontIcon(MaterialDesign.MDI_FOLDER_OUTLINE).addClass("big-icon")
            }

            textflow {
                addClass("audio-missing__text-flow")
                textAlignment = TextAlignment.CENTER

                val textMessage = messages["drag_drop_or_browse_import__template"]
                val prefixText = textMessage.substringBefore('{')
                val suffixText = textMessage.substringAfter('}')

                text(prefixText) {
                    addClass("normal-text", "centered")
                }
                hyperlink(messages["choose_file"]).apply {
                    addClass("wa-text--hyperlink", "audio-missing__link-text")
                    tooltip(text)
                    action {
                        chooseFile(
                            FX.messages["importResourceFromZip"],
                            arrayOf(
                                FileChooser.ExtensionFilter(
                                    messages["oratureFileTypes"],
                                    *OratureFileFormat.extensionList.map { "*.$it" }.toTypedArray()
                                )
                            ),
                            mode = FileChooserMode.Single,
                            owner = currentWindow
                        )//.firstOrNull()?.let { importFile(it) }
                    }
                }
                text(suffixText) {
                    addClass("normal-text", "centered")
                }
            }
        }

        stackpane {
            addClass("audio-missing__separator-area")
            separator { fitToParentWidth() }
        }

        label(messages["need_source_audio"]) {
            addClass("h3")
        }

        textflow {
            addClass("audio-missing__text-flow")

            val textMessage = messages["source_audio_download_description__template"]
            val prefixText = textMessage.substringBefore('{')
            val suffixText = textMessage.substringAfter('}')
            text(prefixText) {
                addClass("normal-text")
            }
            hyperlink("audio.bibleineverylanguage.org").apply {
                addClass("wa-text--hyperlink", "audio-missing__link-text")
                tooltip("audio.bibleineverylanguage.org/gl")
                action {
                    hostServices.showDocument("https://audio.bibleineverylanguage.org/gl")
                }
            }
            text(suffixText) {
                addClass("normal-text")
            }
        }

        hbox {
            addClass("audio-missing__actions")

            button(messages["check_online"]) {
                addClass("btn", "btn--primary")
                tooltip(text)
                graphic = FontIcon(MaterialDesign.MDI_EXPORT)
                prefWidthProperty().bind(this@hbox.widthProperty().divide(2))
                action {
                    hostServices.showDocument("https://audio.bibleineverylanguage.org/gl")
                }
            }

            button {
                addClass("btn", "btn--secondary")
                text = MessageFormat.format(
                    messages["begin_narrating_book"],
                    viewModel.bookTitleProperty.value
                )
                tooltip(text)
                graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                prefWidthProperty().bind(this@hbox.widthProperty().divide(2))
            }
        }
    }
}