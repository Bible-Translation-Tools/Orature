import com.example.demo.view.DatagridDemo
import tornadofx.*;
//Put the view you want before the double colon
class MyApp: App(DatagridDemo::class) {
}
fun main(args: Array<String>) {
    launch<MyApp>(args);
}