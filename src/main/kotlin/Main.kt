import tornadofx.*;
import java.util.*
import kotlin.concurrent.schedule
import view.AppView

//Put the view you want before the double colon
//Sub in other views to test them out by themselves
class MyApp: App(AppView::class)

//launch the app
fun main(args: Array<String>) {
    launch<MyApp>(args);
}