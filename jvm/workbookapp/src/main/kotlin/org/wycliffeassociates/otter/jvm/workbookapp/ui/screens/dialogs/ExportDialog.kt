package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ContributorInfo
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ContributorListCell
import tornadofx.*

class ExportDialog : OtterDialog() {
    private val contributors = observableListOf(Contributor("Tony T."), Contributor("Jonathan T."), Contributor("Joel S."))
    var contributorField: TextField by singleAssign()

    private val content = ContributorInfo()

    init {
        setContent(content)
        open()
    }
}