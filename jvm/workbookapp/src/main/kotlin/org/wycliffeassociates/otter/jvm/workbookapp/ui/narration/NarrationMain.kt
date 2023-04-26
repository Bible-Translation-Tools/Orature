import javafx.stage.Stage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.NarrationView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class NarrationApp(): App(NarrationView::class) {

    val workbookDataStore by inject<WorkbookDataStore>()

    init {
        val workbook = mock<WorkbookDataStore>()
        workbookDataStore.setWorkbook(workbook)
    }


    override fun start(stage: Stage) {
        super.start(stage)
        stage.isMaximized = true
    }
}

fun main() {
    launch<NarrationApp>()
}