package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ContributorInfo
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class ExportDialog : OtterDialog() {
    var contributorField: TextField by singleAssign()

    private val settingsViewModel: SettingsViewModel by inject()

    private val content = VBox().apply {
        addClass("contributor-dialog")

        button {
            addClass("add-plugin-dialog__btn--close") // TODO: refactor to common style class
            tooltip(messages["close"])
            graphic = FontIcon("gmi-close")
            action { close() }
        }
        add(ContributorInfo())
    }

    init {
        setContent(content)
        open()
    }

    override fun onDock() {
        super.onDock()
        themeProperty.set(settingsViewModel.appColorMode.value)
    }
}