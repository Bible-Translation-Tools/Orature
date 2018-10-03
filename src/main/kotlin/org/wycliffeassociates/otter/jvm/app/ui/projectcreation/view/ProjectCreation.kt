package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.event.ActionEvent
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.SelectBook
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.SelectLanguage
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.SelectResource
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.SelectAnthology
import org.wycliffeassociates.otter.jvm.app.widgets.progressstepper.ProgressStepper
import tornadofx.*

class ProjectCreationWizard: Wizard() {

    val steps = listOf(MaterialIconView(MaterialIcon.RECORD_VOICE_OVER, "16px"),
            MaterialIconView(MaterialIcon.COLLECTIONS_BOOKMARK, "16px"),
            MaterialIconView(MaterialIcon.CROP_SQUARE, "16px"),MaterialIconView(MaterialIcon.BOOK, "16px"))
    override  val canGoNext = currentPageComplete
    init {
        showStepsHeader = false
        showStepsHeader = false
        showSteps = false
        showHeader = true
        enableStepLinks = true
        root.top =
            ProgressStepper(steps).apply {
                currentPageProperty.onChange {
                    nextView(pages.indexOf(currentPage))
                }
                addEventHandler(ActionEvent.ACTION) {
                    currentPage = pages[activeIndexProperty]
                }
            }

        add(SelectLanguage::class)
        add(SelectResource::class)
        add(SelectAnthology::class)
        add(SelectBook::class)
    }
}