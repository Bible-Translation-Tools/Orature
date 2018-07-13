package view;
import tornadofx.*;
import java.util.*
import kotlin.concurrent.schedule
import kotlin.reflect.KClass

val recordView1 = RecordView1();
val recordView2 = RecordView2();
val recordView3 = RecordView3();
var index = 0;
var currView: KClass<out View>  = RecordView1::class;
class AppView: View() {
    val myList: List<KClass<out View>> = listOf(RecordView2::class, RecordView3::class);
    override val root = borderpane {
        center<RecordView1>();
        val myTimer = Timer();
        class SwapView: TimerTask() {
            override fun run() {
                if(index < myList.size) {
                    currView = myList[index];
                    println(replaceWith(myList[index], null, false, false));
                    index += 1;
                } else {
                    myTimer.cancel();
                }
            }
        }
        myTimer.schedule(SwapView(), 1000);
        myTimer.schedule(SwapView(), 1500);
        myTimer.schedule(SwapView(), 2000);
    }

}