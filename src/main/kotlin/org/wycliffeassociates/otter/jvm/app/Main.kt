package org.wycliffeassociates.otter.jvm.app

import org.wycliffeassociates.otter.jvm.app.ui.profilePreview.View.ProfilePreview
import org.wycliffeassociates.otter.jvm.app.ui.welcomeScreen.*
import tornadofx.*
import java.util.*

class MyApp : App(WelcomeScreen::class)
//launch the org.wycliffeassociates.otter.jvm.app
fun main(args: Array<String>) {
    launch<MyApp>(args)
}