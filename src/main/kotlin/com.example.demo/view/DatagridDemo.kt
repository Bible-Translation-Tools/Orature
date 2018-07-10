package com.example.demo.view
import com.example.demo.controller.DataService
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*;

class DatagridDemo: View("Datagrid Demo") {

    val data: DataService by inject();

    val myCurrentUser = borderpane () {
        val mycuTopWidget = TopWidget(100.0);
        //alignment must be set on root, not on Widget itself
        mycuTopWidget.root.alignment = Pos.BOTTOM_CENTER;
        //set the grow property or the root will change size but not the content
        HBox.setHgrow(mycuTopWidget.root, Priority.ALWAYS);
        mycuTopWidget.root.setPrefSize(200.0, 200.0);
        usePrefSize;
        //areas of borderpane can be set to root of View/Frag
        top = mycuTopWidget.root;

        center = label("Welcome!");

        val myBottomWidget = BottomWidget();
        myBottomWidget.root.alignment = Pos.TOP_CENTER;
        bottom = myBottomWidget.root;

        setPrefSize(400.0, 400.0);
        usePrefSize;
    }

    val myDatagrid = datagrid(data.numbers()) {
        cellFormat {
            graphic = borderpane() {
                val mydgTopWidget = TopWidget(25.0);
                mydgTopWidget.root.alignment = Pos.CENTER;
                top = mydgTopWidget.root;

                center = label("user " + it);

                val mydgBottomWidget = BottomWidget();
                mydgBottomWidget.root.alignment = Pos.CENTER;
                bottom = mydgBottomWidget.root;
            }
        }
        setPrefSize(200.0, 200.0);
        usePrefSize;
    }

    val welcomeScreen = borderpane() {
        BorderPane.setMargin(myCurrentUser, Insets(20.0, 20.0, 20.0, 20.0));
        BorderPane.setMargin(myDatagrid, Insets(20.0, 20.0, 20.0, 20.0));
        BorderPane.setAlignment(myCurrentUser, Pos.BOTTOM_CENTER)
        left = myCurrentUser;
        right = myDatagrid;
    }

    override val root = welcomeScreen;

}

