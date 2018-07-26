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
import io.reactivex.rxkotlin.subscribeBy
import app.ui.profilePreview.View.ProfilePreview

/*
*  This View is used in the process of creating user profiles
*  This View (UserCreation) Which serves as the parent view
*  has two children. A close button, and a recordingFrame/recordButton that
*  gives the user visual feedback through the recording process
*
* */
class UserCreation : View() {



    private val mIcon = MaterialIconView(MaterialIcon.CLOSE, "25px")
    private val viewModel: UserCreationViewModel  by inject()
    var recordButton = RecordButton(::onClickCallback, ::animationCompleted, ::stopClicked)
    private val progressBar = ProgressBar()
    val isRecording = viewModel.isRecording
    val doneRecording = viewModel.doneRecording
    var timer = Timer()
    val RECORDING_DONE: Long = 6100


    //initialize close button to be used in top of borderpane
    val closeButton = button(messages["close"], mIcon) {
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

    override val root = borderpane {

        //nodeOrientationProperty().value = NodeOrientation.RIGHT_TO_LEFT

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

            isRecording.subscribeBy(
                    onNext = {
                        if (it == false) { // if isRecording set to false create new RecordButton()
                            val newRecordButton = RecordButton(::onClickCallback, ::animationCompleted, ::stopClicked)
                            newRecordButton.alignment = Pos.CENTER
                            recordButton.replaceWith(newRecordButton)
                            recordButton = newRecordButton
                        }
                    },

                    onError = {
                        println(it)
                    }
            )

            doneRecording.subscribeBy(
                    onNext = {
                        if (it == true) { //done recording = true? then navigate to profile preview
                            timer.schedule(timerTask {
                                Platform.runLater {
                                    find(UserCreation::class).replaceWith(ProfilePreview::class,
                                            transition = ViewTransition.Fade(0.3.seconds))
                                }
                            }, 500)

                        }
                    },

                    onError = {
                        println(it)
                    }
            )

            add(recordButton)
            recordButton.alignment = Pos.CENTER

            style {
                backgroundColor += Color.TRANSPARENT
                borderColor += box(Color.TRANSPARENT)
            }
        }
    }

    override fun onUndock() { //clean up on view exit
        viewModel.reset()
        timer.cancel()
        timer.purge()

    }

    override fun onDock() { //set up on view entry
        timer = Timer()
    }

    private fun navHome() {

        find(UserCreation::class).replaceWith(WelcomeScreen::class)
    }

    private fun animationCompleted() {
        TODO("add code that runs when countdown animation is completed. Probably being audio recording" +
                "function name might change")
        //viewModel.reset()
    }

    private fun stopClicked() {
        find(UserCreation::class).replaceWith(ProfilePreview::class,
                transition = ViewTransition.Fade(0.3.seconds))
    }

    private fun onClickCallback() {
        viewModel.recordAudio()
        viewModel.recordClicked()
        timer.schedule(timerTask {
            Platform.runLater {
                viewModel.doneRecording()
            }
        }, RECORDING_DONE)
    }

}

