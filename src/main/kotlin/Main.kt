import tornadofx.*;

class MyApp: App(AppView::class)

//launch the app
fun main(args: Array<String>) {
    launch<MyApp>(args);
}