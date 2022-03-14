package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ContributorInfo
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterPageViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class ExportDialog : OtterDialog() {
    var contributorField: TextField by singleAssign()
    private val viewModel: ChapterPageViewModel by inject()

    private val settingsViewModel: SettingsViewModel by inject()

    private val content = VBox().apply {
        addClass("contributor-dialog")

        vbox {
            hbox {
                paddingLeft = 30.0
                paddingTop = 20.0
                paddingBottom = -20.0
                label (messages["exportChapter"]) {
                    alignment = Pos.CENTER
                    addClass("contributor__section-title")
                }
            }
            hbox {
                add(ContributorInfo())
                button {
                    addClass("btn", "btn--icon","contributor__list-cell__close-btn .ikonli-font-icon") // TODO: refactor to common style class
                    tooltip(messages["close"])
                    graphic = FontIcon("gmi-close")
                    action { close() }
                }
            }
        }
        hbox {
            spacing = 20.0
            paddingBottom = 20.0
            paddingTop = -20.0
            alignment = Pos.CENTER

            button (messages["exportChapter"]) {
                addClass("btn--primary","btn--borderless")
                graphic = FontIcon(Material.UPLOAD_FILE)
                hgrow = Priority.ALWAYS
                action {
                    export()
                }
            }
            button (messages["cancel"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon("gmi-close")
                hgrow = Priority.SOMETIMES
                action {
                    close()
                }
            }
        }
    }

    init {
        setContent(content)
        open()
    }

    fun export() {
        val directory = chooseDirectory(FX.messages["exportChapter"])
        directory?.let {
            viewModel.exportChapter(it)
        }
    }

    override fun onDock() {
        super.onDock()
        themeProperty.set(settingsViewModel.appColorMode.value)
    }
}