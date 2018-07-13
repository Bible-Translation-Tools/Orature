package app

import app.ui.MainView
import tornadofx.App
import tornadofx.launch
import widgets.createNewProfileButton.view.CreateNewProfileButtonStyle

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        launch<MainApp>()
    }
}

class MainApp : App(MainView:: class, CreateNewProfileButtonStyle:: class)