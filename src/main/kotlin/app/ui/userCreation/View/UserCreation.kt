package app.ui.userCreation

import app.ui.styles.ButtonStyles
import app.ui.userCreation.ViewModel.UserCreationViewModel
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*
import app.ui.widgets.recordButton.RecordButton
import app.ui.welcomeScreen.*
import com.github.thomasnield.rxkotlinfx.actionEvents
import javafx.application.Platform
import java.util.*
import kotlin.concurrent.timerTask
import app.ui.ProgressBar



class UserCreation : View("My View") {
    val mIcon = MaterialIconView(MaterialIcon.CLOSE, "25px")
    val UserCreationViewModel : UserCreationViewModel  by inject ()
    val countdown = UserCreationViewModel.countdownTracker
    val recordButton = RecordButton()


    override val root = borderpane {

        style{
            backgroundColor+= Color.WHITE
        }

        fun navHome() {
            find(UserCreation::class).replaceWith(WelcomeScreen::class, ViewTransition.Slide(.9.seconds, ViewTransition.Direction.RIGHT))
        }

        val closeButton = button("CLOSE",mIcon) {
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

        top {

            hbox {
                alignment = Pos.BOTTOM_RIGHT
                add(closeButton)
                style {
                    alignment = Pos.BOTTOM_RIGHT
                    paddingRight= 40.0
                    paddingTop = 40.0
                }
            }

        }
        center{

            button {

                add(recordButton)
                recordButton.alignment = Pos.CENTER

                style {
                    backgroundColor += Color.TRANSPARENT
                    borderColor+= box(Color.TRANSPARENT)
                }

                action {
                  //  UserCreationViewModel.countdown()
                    var timer = Timer()
                    timer.schedule(timerTask {
                        Platform.runLater {
                            replaceWith(ProgressBar(), transition = ViewTransition.Fade(.2.seconds))

                        }
                    }, 6100)
                }
            }

            //actionEvents().subscribe(UserCreationViewModel.countdown())
        }
    }
}

