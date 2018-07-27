package app.widgets.recordButton


import app.MyApp.Companion.Colors
import app.ui.styles.ButtonStyles
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*


import java.util.*
import kotlin.concurrent.timerTask

class RecordButton(var onClickCallback: () -> Unit = ::println, var animationCompletedCallback: () -> Unit = ::println, var stopClickedCallback: () -> Unit = ::println) : VBox() {

    val circleAnimation = RecordingAnimation()
    val dotsAnimation = DotsAnimation()
    val RecordButtonViewModel = RecordButtonViewModel()
    val countdown = RecordButtonViewModel.countdownTracker
    val isRecording = RecordButtonViewModel.isRecording
    val micIcon = MaterialIconView(MaterialIcon.MIC_NONE, "100px")
    val stopIcon = MaterialIconView(MaterialIcon.STOP, "100px")

    var timer = Timer()
    val wrapper =
            stackpane {
                alignment = Pos.CENTER

                add(circleAnimation)
                button(countdown, micIcon) {
                    importStylesheet(ButtonStyles::class)
                    addClass(ButtonStyles.roundButton)
                    style {
                        backgroundColor += c(Colors["base"])
                        micIcon.fill = c(Colors["accent"])
                        cursor = Cursor.HAND
                        minWidth = 152.0.px
                        minHeight = 152.0.px
                        fontSize = 8.em
                        textFill = c(Colors["accent"])
                    }

                    action {
                        if (isRecording.value == false) {
                            RecordButtonViewModel.isRecording(true)
                            dotsAnimation.circleCountdown()
                            micIcon.hide()
                            RecordButtonViewModel.countdown()
                            onClickCallback()
                            timer.schedule(timerTask {
                                Platform.runLater {
                                    circleAnimation.animate()
                                    graphic = stopIcon
                                    stopIcon.fill =c(Colors["accent"])
                                    dotsAnimation.hide()
                                    //animationCompleted()
                                }
                            }, 3000)
                        } else {
                            if (countdown.value == "") {
                                /*if countdown.value = "" that means the countdown has finished
                                * therefore now the user is able to click stop to record
                                * */
                                circleAnimation.stop()
                                stopClickedCallback()
                            }
                        }
                    }
                }
            }

    val dotsWrapper = hbox {
        alignment = Pos.CENTER
        style {
            padding = box(20.px)
        }
        add(dotsAnimation)
    }

}
