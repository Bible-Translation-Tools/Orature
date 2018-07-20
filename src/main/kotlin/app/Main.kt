package app

import app.ui.welcomeScreen.*
import tornadofx.*;

//Put the view you want before the double colon
//Sub in other views to test them out by themselves
class MyApp: App(WelcomeScreen::class)

//launch the app
fun main(args: Array<String>) {
    launch<MyApp>(args)
}