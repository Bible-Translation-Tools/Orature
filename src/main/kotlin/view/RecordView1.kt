package view

import tornadofx.*;
import javafx.scene.layout.HBox

class RecordView  {
    var one = HBox();
    var two = HBox();
    var three = HBox();
    var myListOf = listOf(one, two, three);

    init{
        one.label("hullo");
        two.label("there");
        three.label("You");
    }
}