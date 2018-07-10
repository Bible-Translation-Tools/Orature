package com.example.demo.view
import com.example.demo.controller.DataService
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import tornadofx.*;

class DatagridDemo: View("Datagrid Demo") {

    //grab the usernames from outside
    //in the real thing, we'll grab icons and sound clips instead
    private val data: DataService by inject();

    //the left half of the screen, which displays:
    //the last user to log in, a welcome message, and a button to go to that user's home
    private val bigIcons = borderpane () {
        //make a big user icon
        val myBigUserIcon = UserIconWidget(100.0);
        //set its alignment to center it
        //alignment must be set on root, not on Widget itself
        myBigUserIcon.root.alignment = Pos.CENTER;

        //if you want to set the size of the box surrounding the elements, I think this works:
        //tell the icon how big it should be
//        myBigUserIcon.root.setPrefSize(200.0, 200.0);
        //tell it that it should definitely be the size you just told it
        //(not sure under which circumstances it wouldn't be, but just to be safe)
//        usePrefSize;
        //areas of borderpane can be set to root of View/Frag
        //you can't set areas of the borderpane to Views/Frags themselves because they aren't nodes
        top = myBigUserIcon.root;

        center = label("Welcome!");

        val myHomeWidget = HomeWidget();
        myHomeWidget.root.alignment = Pos.CENTER;
        bottom = myHomeWidget.root;
    }

    //the grid of users
    //hooked up to the list we pulled in up top from DataService
    //right now it has just 6 elems but the real one will have who-knows-how-many
    private val myGrid = datagrid(data.numbers()) {
        //formats each cell; if not called, cells are just empty white squares
        //the "it" inside is an item from data.numbers
        cellFormat {
            //each cell is a borderpane
            graphic = borderpane() {
                //make a small icon
                val currentSmallUserIcon = UserIconWidget(25.0);
                //set its alignment to center so it shows up in the middle of the cell
                //(otherwise shows up in left)
                currentSmallUserIcon.root.alignment = Pos.CENTER;
                //set it to an area of the borderpane
                top = currentSmallUserIcon.root;

                center = label("user " + it);

                val mydgBottomWidget = HomeWidget();
                mydgBottomWidget.root.alignment = Pos.CENTER;
                bottom = mydgBottomWidget.root;
            }
        }
        setPrefSize(200.0, 200.0);
        usePrefSize;
    }

    private val welcomeScreen = borderpane() {
        //put in some nice margins so it's not too crowded
        BorderPane.setMargin(bigIcons, Insets(20.0, 20.0, 20.0, 20.0));
        BorderPane.setMargin(myGrid, Insets(20.0, 20.0, 20.0, 20.0));
        //BorderPane.setAlignment(bigIcons, Pos.CENTER)
        //put the elems in the welcomeScreen
        left = bigIcons;
        right = myGrid;
    }

    //set the root of the view to the welcomeScreen
    override val root = welcomeScreen;

}

