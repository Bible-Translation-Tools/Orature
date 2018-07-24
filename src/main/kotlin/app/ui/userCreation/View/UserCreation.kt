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



class UserCreation : View("Creating User") {


    private val mIcon = MaterialIconView(MaterialIcon.CLOSE, "25px")
    private val viewModel : UserCreationViewModel  by inject ()
    private var recordButton = RecordButton()
    private val progressBar = ProgressBar()
    val isRecording = viewModel.isRecording
    val doneRecording = viewModel.doneRecording
    var timer = Timer()

    //initialize close button to be used in top of borderpane
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

    override val root = borderpane {

        style{
            backgroundColor+= Color.WHITE
        }

        top {

            //close button
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
            // I wrap the record button inside another button in order to able to be able to use the button's action tag
            button {

                isRecording.subscribeBy(
                        onNext = {
                            if (it == false) { // if not recording provide a new recordButton Widget, wrapped in button for
                                                //same as reason as above
                                progressBar.hide()
                                val newRecordButton = RecordButton()
                                newRecordButton.alignment = Pos.CENTER
                                val newButton = button {
                                    add(newRecordButton)
                                    style {
                                        backgroundColor += Color.TRANSPARENT
                                        borderColor+= box(Color.TRANSPARENT)
                                    }
                                }
                                progressBar.replaceWith(newButton)
                            }
                        }
                )

                doneRecording.subscribeBy(
                        onNext = {

                            if(it == true) {
                                //recordButton.replaceWith(progressBar, transition = ViewTransition.Fade(0.2.seconds))

                                timer.schedule(timerTask {
                                    Platform.runLater {
                                        find(UserCreation::class).replaceWith(ProfilePreview::class, transition = ViewTransition.Fade(0.3.seconds))
                                    }
                                }, 500)

                            }
                        }
                )
              add(progressBar)
              progressBar.hide()

              add(recordButton)
                recordButton.alignment = Pos.CENTER

                style {
                    backgroundColor += Color.TRANSPARENT
                    borderColor+= box(Color.TRANSPARENT)
                }

                action {
                    viewModel.recordAudio()
                    viewModel.recordClicked()
                    timer.schedule(timerTask {
                        Platform.runLater {
                            viewModel.doneRecording()
                        }
                    }, 6100)
                }
            }
        }
    }

    override fun onUndock() {
        viewModel.reset()
        timer.cancel()
        timer.purge()
    }

    override fun onDock() {
        timer = Timer()
    }

    fun navHome() {

        find(UserCreation::class).replaceWith(WelcomeScreen::class)
        viewModel.reset()

    }
}

