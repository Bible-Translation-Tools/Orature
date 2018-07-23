package app.ui.userCreation

import app.ui.styles.ButtonStyles
import app.ui.userCreation.ViewModel.UserCreationViewModel
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*
import app.widgets.recordButton.RecordButton
import app.ui.welcomeScreen.*
import javafx.application.Platform
import java.util.*
import kotlin.concurrent.timerTask
import app.ui.ProgressBar
import com.github.thomasnield.rxkotlinfx.actionEvents
import io.reactivex.rxkotlin.subscribeBy
import javafx.animation.Transition
import java.awt.event.ActionEvent
import app.ui.profilePreview.View.ProfilePreview



class UserCreation : View("Creating User") {

    val UserCreationViewModel = UserCreationViewModel()
    val countdown = UserCreationViewModel.countdownTracker
    val mIcon = MaterialIconView(MaterialIcon.CLOSE, "25px")
    val ViewModel : UserCreationViewModel  by inject ()
    var recordButton = RecordButton()
    val progressBar = ProgressBar()
    val example = ViewModel.isRecording
    val doneRecording = ViewModel.doneRecording





    override val root = borderpane {

        style{
            backgroundColor+= Color.WHITE
        }

        fun navHome() {

            find(UserCreation::class).replaceWith(WelcomeScreen::class)
            ViewModel.reset()

        }

        val closeButton = button("CLOSE", mIcon) {
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

                example.subscribeBy(
                        onNext = {
                            if (it == false) {
                                recordButton = RecordButton()
                                recordButton.alignment = Pos.CENTER
                                progressBar.replaceWith(recordButton)
                            }
                        }
                )

                doneRecording.subscribeBy(
                        onNext = {
                            if(it == true) {
                                replaceWith(progressBar, transition = ViewTransition.Fade(0.2.seconds))

                                var timer = Timer()
                                timer.schedule(timerTask {
                                    Platform.runLater {
                                        find(UserCreation::class).replaceWith(ProfilePreview::class, transition = ViewTransition.NewsFlash(0.5.seconds))
                                    }
                                }, 1000)


                            }

                        }
                )


                add(recordButton)
                recordButton.alignment = Pos.CENTER

                style {
                    backgroundColor += Color.TRANSPARENT
                    borderColor+= box(Color.TRANSPARENT)
                }

                action {
                    ViewModel.recordAudio()
                    ViewModel.recordClicked()
                    var timer = Timer()
                    timer.schedule(timerTask {
                        Platform.runLater {
                            ViewModel.doneRecording()
                        }
                    }, 6100)
                }
            }
        }



    }

    override fun onUndock() {
        ViewModel.reset()
    }

}

