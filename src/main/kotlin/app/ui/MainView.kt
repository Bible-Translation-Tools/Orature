package app.ui

import tornadofx.*
import widgets.createNewProfileButton.view.CreateNewProfileButton

class MainView : View() {
    override val root = stackpane {
        add(CreateNewProfileButton())
    }
}