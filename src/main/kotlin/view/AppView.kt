import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import tornadofx.*;
import view.ViewMine;
import java.util.concurrent.TimeUnit

class AppView : View() {
    val myV = ViewMine();

//    // an object that holds obervable disposables
//    val compositeDisposable = CompositeDisposable()

    val myBox = hbox() {
//        //make an observable that emits a signal ever 1000 milliseconds (i.e. 1 second)
//        val source = Observable.interval(1000, TimeUnit.MILLISECONDS)
//                //the observable emmits a signal 3 times
//                .take(3);
//
//        //our disposable
//        val trash = source
//                //We're forcing the subscriber to be a part of the FX thread so that another thread (the one the subscriber
//                //would be on without "observeOnFx") doesn't mess up the FX thread.
//                //(Independent threads trying to influence each other usually causes errors.)
//                .observeOnFx()
//                //we make our subscriber
//                .subscribe {
//                    if(it == "0".toLong()) {
////                        myLabel.text = "hey";
//                    } else {
////                        myLabel.text = "there";
//                    }
//        }
//
//        // add trash to list of disposables
//        compositeDisposable.add(trash)

    }

    override val root = myV;

    //executes when app closes
    override fun onUndock() {
        // add trash to list of disposables
        myV.compositeDisposable.add(myV.trash)
        super.onUndock()
        myV.compositeDisposable.clear()
    }
}