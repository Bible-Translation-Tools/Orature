package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import org.wycliffeassociates.otter.jvm.controls.RollingSourceText
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class ControlsDemoView : View() {
    private val text =
        "1. It happened in the days when the judges ruled that there was a famine in the land, and a certain man of Bethlehem of Judah went to live as a foreigner in the country of Moab with his wife and his two sons.\n" +
            "2. The name of the man was Elimelek, and the name of his wife was Naomi. The names of his two sons were Mahlon and Kilion, who were Ephrathites of Bethlehem of Judah. They arrived at the country of Moab and lived there.\n" +
            "3. Then Elimelek, Naomi's husband, died, and she was left alone with her two sons.\n" +
            "4. These sons took wives from the women of Moab; the name of one was Orpah, and the name of the other was Ruth. They lived there for about ten years.\n" +
            "5. Then both Mahlon and Kilion died, and the woman was left without her two sons and without her husband.\n"

    override val root =
        vbox {
            maxWidth = 300.0

            val node =
                RollingSourceText().apply {
                    sourceTitleProperty.set("English ULB")
                    sourceTextProperty.set(text)
                    licenseTextProperty.set("Example of license text")
                }
            add(node)
        }

    init {
        tryImportStylesheet("/css/source-content.css")
    }
}
