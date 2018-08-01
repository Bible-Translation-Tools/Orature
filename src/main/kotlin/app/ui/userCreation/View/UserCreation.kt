package app.ui.userCreation

import app.UIColorsObject.Colors
import app.ui.userCreation.ViewModel.UserCreationViewModel
import app.ui.welcomeScreen.*
import app.widgets.recordButton.RecordingAnimation
import app.widgets.recordButton.DotsAnimation
import app.ui.profilePreview.View.ProfilePreview
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*
import javafx.application.Platform
import java.util.*
import kotlin.concurrent.timerTask
import app.ui.ProgressBar
import javafx.scene.Cursor
import javafx.scene.control.Button
import app.widgets.WidgetsStyles
import javafx.scene.shape.Circle


/*
*  This View is used in the process of creating user profiles
*  This View (UserCreation) Which serves as the parent view
*  has two children. A close button, and a recordingFrame/recordButton that
*  gives the user visual feedback through the recording process
*
* */
class UserCreation : View() {

    private val closeIcon = MaterialIconView(MaterialIcon.CLOSE, "25px")
    private val viewModel: UserCreationViewModel  by inject()
    private val circleAnimation = RecordingAnimation()
    private val dotsAnimation = DotsAnimation()
    private val countdown = viewModel.countdownTracker
    private val progressBar = ProgressBar()
    val isRecording = viewModel.isRecording
    val doneRecording = viewModel.doneRecording
    val micIcon = MaterialIconView(MaterialIcon.MIC_NONE, "100px")
    val stopIcon = MaterialIconView(MaterialIcon.STOP, "100px")
    var timer = Timer()
    val RECORDING_TIME: Long = 6100
    val COUNTDOWN_TIME: Long = 3000
    var recordButton: Button? = null

    //initialize close button to be used in top of borderpane
    val closeButton = button(messages["close"], closeIcon) {
        importStylesheet(WidgetsStyles::class)
        addClass(WidgetsStyles.rectangleButtonDefault)

        style {
            alignment = Pos.CENTER
            closeIcon.fill = c(Colors["primary"])
            effect = DropShadow(10.0, Color.GRAY)
        }
        action {
            navHome()
        }
    }
    override val root = borderpane {
        style {
            backgroundColor += Color.WHITE
        }
        top {
            //close button
            hbox {
                alignment = Pos.BOTTOM_RIGHT
                add(closeButton)
                style {
                    alignment = Pos.BOTTOM_RIGHT
                    paddingRight = 40.0
                    paddingTop = 40.0
                }
            }
        }
        center {

            vbox(8) {
                alignment = Pos.CENTER
                stackpane {
                       alignment=Pos.CENTER
                           add(circleAnimation)
                           circleAnimation.children.style {
                               fill = c(Colors["baseBackground"])
                           }
                    recordButton = button(countdown, graphic = micIcon) {
                        importStylesheet(WidgetsStyles::class)
                        addClass(WidgetsStyles.roundButton)
                        style {
                            backgroundColor += c(Colors["base"])
                            micIcon.fill = c(Colors["primary"])
                            cursor = Cursor.HAND
                            minWidth = 152.0.px
                            minHeight = 152.0.px
                            fontSize = 8.em
                            textFill = c(Colors["primary"])
                            fill = c(Colors["primary"])
                        }
                        action {
                            if (isRecording.value == false) {
                                graphic.hide()
                                dotsAnimation.circleCountdown(Colors["primary"])
                                viewModel.countdown()
                                viewModel.recordClicked()
                                timer.schedule(timerTask {
                                    Platform.runLater {
                                        circleAnimation.animate(Colors["primary"])
                                        stopIcon.fill = c(Colors["primary"])
                                        graphic = stopIcon
                                        dotsAnimation.hide()
                                        graphic.show()
                                    }
                                }, COUNTDOWN_TIME)
                                timer.schedule(timerTask {
                                    Platform.runLater {
                                        find(UserCreation::class).replaceWith(ProfilePreview::class)
                                        val newMicIcon = MaterialIconView(MaterialIcon.MIC_NONE, "100px")
                                        newMicIcon.fill = c(Colors["primary"])
                                        graphic = newMicIcon
                                        dotsAnimation.show()
                                    }
                                }, RECORDING_TIME)
                            } else if (countdown.value == "") {
                                dotsAnimation.show()
                                circleAnimation.stop()
                                val newMicIcon = MaterialIconView(MaterialIcon.MIC_NONE, "100px")
                                newMicIcon.fill = c(Colors["primary"])
                                graphic = newMicIcon
                                find(UserCreation::class).replaceWith(ProfilePreview::class)
                            }
                        }
                    }
                }
                add(dotsAnimation)
                dotsAnimation.children.style {
                    fill = c(Colors["baseBackground"])
                }
            }
        }
    }

    override fun onUndock() { //clean up on view exit
        viewModel.reset()
        viewModel.stopCountdown()
        circleAnimation.stop()
        circleAnimation.reset()
        dotsAnimation.resetCircles(Colors["baseBackground"])
        timer.cancel()
        timer.purge()
    }

    override fun onDock() { //set up on view entry
        timer = Timer()
    }

    private fun navHome() {
        find(UserCreation::class).replaceWith(WelcomeScreen::class)
        dotsAnimation.show()
        val newMicIcon = MaterialIconView(MaterialIcon.MIC_NONE, "100px")
        newMicIcon.fill = c(Colors["primary"])
        recordButton?.graphic = newMicIcon
    }

    private fun animationCompleted() {
        // TODO("add code that runs when countdown animation is completed. Probably being audio recording" +
        //         "function name might change")
        //viewModel.reset()
    }
}