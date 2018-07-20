package app.ui.userCreation

import app.ui.styles.ButtonStyles
import app.ui.ProgressBar
import app.ui.userCreation.ViewModel.UserCreationViewModel
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*
import app.ui.widgets.recordButton.RecordButton
import app.ui.welcomeScreen.*
import javafx.application.Platform
import java.util.*
import kotlin.concurrent.timerTask


class UserCreation : View("Creating User") {

    val UserCreationViewModel = UserCreationViewModel()
    val countdown = UserCreationViewModel.countdownTracker
    val mIcon = MaterialIconView(MaterialIcon.CLOSE, "25px")
    val stopIcon = MaterialIconView(MaterialIcon.STOP, "100px")





    override val root = borderpane {

        style{
            backgroundColor+= Color.WHITE
        }

        fun navHome() {
            find(UserCreation::class).replaceWith(WelcomeScreen::class, ViewTransition.Slide(.9.seconds, ViewTransition.Direction.RIGHT))
        }

        val CloseButton = button("CLOSE",mIcon) {
            importStylesheet(ButtonStyles::class)
            addClass(ButtonStyles.rectangleButtonDefault)

            style {
                alignment = Pos.CENTER
                mIcon.fill = c("#CC4141")
                effect = DropShadow(10.0, Color.GRAY)

            }
            action {

                navHome()
            }
        }

        val recordButton = RecordButton()

        top {

            hbox {
                alignment = Pos.BOTTOM_RIGHT
                add(CloseButton)
                style {
                    alignment = Pos.BOTTOM_RIGHT
                    paddingRight= 40.0
                    paddingTop = 40.0

                }
            }

        }
        center{

            add(recordButton)
            recordButton.alignment = Pos.CENTER

        }



    }


}

