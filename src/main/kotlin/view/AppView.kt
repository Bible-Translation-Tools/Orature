package view;
import tornadofx.*;
import java.util.*
import kotlin.concurrent.schedule

class AppView: View() {
    val recordView1 = RecordView1();
    val recordView2 = RecordView2();
    val recordView3 = RecordView3();
    val myList = listOf(recordView2, recordView3);
    override val root = borderpane {
        val i = 0;
        center = recordView1.root;
        println("1")
        val myTimer = Timer();
        class SwapView: TimerTask() {
            var i = 0;
            override fun run() {
                replaceWith(recordView2.root)
            }
        }
        myTimer.schedule(SwapView(), 1000)
        /*
        Timer("SettingUp", false).schedule(500) {
            replaceWith(recordView2.root)
            println("2")
        }
        Timer("SettingUp", false).schedule(600) {
            replaceWith(recordView1.root)
            println("3")
        }
        */
    }

}