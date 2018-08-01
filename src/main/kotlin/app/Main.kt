package app

import app.ui.profilePreview.View.ProfilePreview
import app.ui.welcomeScreen.*
import tornadofx.*
import java.util.*

class MyApp : App(WelcomeScreen::class)

//launch the app
fun main(args: Array<String>) {
    launch<MyApp>(args)
}