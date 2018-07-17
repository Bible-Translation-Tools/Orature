package view

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import tornadofx.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color
import java.util.concurrent.TimeUnit

class ViewMine: Pane()  {

    val one = circle{
        centerX = 30.0;
        centerY = 20.0;
        radius = 20.0;
        fill = Color.CORNFLOWERBLUE;
    }

    val two = circle{
        centerX = 70.0;
        centerY = 20.0;
        radius = 20.0;
        fill = Color.MEDIUMSEAGREEN;
    }

    val three = circle{
        centerX = 110.0;
        centerY = 20.0;
        radius = 20.0;
        fill = Color.OLIVE;
    }

    // an object that holds obervable disposables
    val compositeDisposable = CompositeDisposable()

    //make an observable that emits a signal ever 1000 milliseconds (i.e. 1 second)
    val source = Observable.interval(1000, TimeUnit.MILLISECONDS)
            //the observable emmits a signal 3 times
            .take(3);

    //our disposable
    val trash = source
            //We're forcing the subscriber to be a part of the FX thread so that another thread (the one the subscriber
            //would be on without "observeOnFx") doesn't mess up the FX thread.
            //(Independent threads trying to influence each other usually causes errors.)
            .observeOnFx()
            //we make our subscriber
            .subscribe {
                if (it == "0".toLong()) {
                    circle{
                        centerX = 30.0;
                        centerY = 20.0;
                        radius = 21.0;
                        fill = Color.WHITE;
                    }
                } else if (it == "1".toLong()) {
                    circle{
                        centerX = 70.0;
                        centerY = 20.0;
                        radius = 21.0;
                        fill = Color.WHITE;
                    }
                } else if (it == "2".toLong()) {
                    circle{
                        centerX = 110.0;
                        centerY = 20.0;
                        radius = 21.0;
                        fill = Color.WHITE;
                    }
                }
            }

}