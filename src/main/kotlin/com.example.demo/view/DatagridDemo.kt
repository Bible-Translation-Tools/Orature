package com.example.demo.view
import com.example.demo.controller.DataService
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import tornadofx.*;
import widgets.profileIcon.view.ProfileIcon

class DatagridDemo: View("Datagrid Demo") {



    //grab the usernames from outside
    //in the real thing, we'll grab icons and sound clips instead
    //so replace this injection with whatever injection you do
    private val data: DataService by inject();

    //the left half of the screen, which displays:
    //the last user to log in, a welcome message, and a button to go to that user's home
    private val rad = 100.0
    private val bigIcons = borderpane () {
        style{
            backgroundColor += Color.valueOf("#FFFFFF")
        }
        //make a big user icon
        val myBigUserIcon = UserIconWidget(rad)

        val iconHash = ProfileIcon("12345678901")

        //set its alignment to center it
        //alignment must be set on root, not on Widget itself
        //myBigUserIcon.root.alignment = Pos.CENTER

        iconHash.alignment = Pos.CENTER


        val myHomeWidget = HomeWidget()
        //set its alignment to center it
        //alignment must be set on root, not on Widget itself
        myHomeWidget.alignment = Pos.CENTER

        top = iconHash
        center = label("Welcome Back!")
        bottom = myHomeWidget

        //prevents from spreading out to take up whole screen when window maximized
        //note: 100 extra pixels hard coded in for space,
        // but we may change this val depending on size of home button and text
        setMaxSize(2 * rad, 3 * rad);
        setPrefSize(2 * rad + 100, 3 * rad);
        usePrefSize
    }

    //the grid of users
    //hooked up to the list we pulled in up top from DataService
    //right now it has just 9 elems but the real one will have who-knows-how-many
    val gridWidth = 600.0;
    private val myGrid = datagrid(data.numbers()) {

        style{
            backgroundColor += Color.valueOf("#DFDEE3")
        }

        //formats each cell; if not called, cells are just empty white squares
        //the "it" inside is an item from data.numbers
        cellFormat {
            //each cell is a borderpane
            graphic = borderpane() {

                style{
                    backgroundColor += Color.valueOf("#DFDEE3")
                }


                //make a small icon
                val randomNumber = Math.floor(Math.random() * 9_000_000_0000L).toLong() + 1_000_000_0000L     // use for demo, replace with DB hash
                val currentSmallUserIcon = ProfileIcon(randomNumber.toString(), 55.0)


                //set its alignment to center so it shows up in the middle of the cell
                //(otherwise shows up in left)
                currentSmallUserIcon.alignment = Pos.CENTER;
                //set it to an area of the borderpane
                top = currentSmallUserIcon;

                //puts a user's number instead of their icon; in the real thing use icon
                center = label("user " + it);

                val currentBottomWidget = HomeWidget();
                currentBottomWidget.alignment = Pos.CENTER;
                bottom = currentBottomWidget;
            }
        }
    }


    private val plusButton = PlusWidget();


    val pad = 60.0;
    private val welcomeScreen = borderpane() {
        left = stackpane{
            add(bigIcons);
        }
        //grid needs to go in center or it won't auto-realign when window resized
        //borderpane gives prefered size to top, bottom, left, and right
        //so resizing gets ignored by top, bottom, left, and right
        center = myGrid;

        //make sure the plus button is in the bottom right
        BorderPane.setAlignment(plusButton.root, Pos.BOTTOM_RIGHT);
        bottom = plusButton.root;
        //put in some nice margins so it's not too crowded
        padding = insets(pad);
    }

    //set the root of the view to the welcomeScreen
    override val root = welcomeScreen;

    //DON'T MOVE THIS TO THE TOP
    //current window will be null unless init goes under setting of root
    init{
        //set minimum size of window so they can always see the last user and the grid of other users
        val minWidth = 3 * pad + 2 * rad + gridWidth;
        //add 100 for home button and Welcome message; probably in real thing these will be vars
        val minHeight = 2 * pad + 2 * rad + 100.0;
        setWindowMinSize(minWidth, minHeight);
    }
}

