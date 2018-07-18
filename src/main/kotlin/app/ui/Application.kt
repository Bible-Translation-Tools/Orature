package app.ui

import tornadofx.App
import tornadofx.launch

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        launch<MainApp>()
    }
}

class MainApp: App(MainView::class)