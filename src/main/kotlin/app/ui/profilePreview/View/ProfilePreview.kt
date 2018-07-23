package app.ui.profilePreview.View
import app.ui.userCreation.*
import app.ui.styles.ButtonStyles
import app.ui.profilePreview.ViewModel.ProfilePreviewViewModel
import app.widgets.profileIcon.ProfileIcon
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import tornadofx.*

class ProfilePreview: View() {

    var iconHash = PublishSubject.create<String>()
    var onClickNext = PublishSubject.create<Boolean>()
    var onClickRedo = PublishSubject.create<Boolean>()
    var audioListened = PublishSubject.create<Boolean>()

    private val viewModel = ProfilePreviewViewModel(iconHash, onClickNext, onClickRedo, audioListened )

    var NewUserButton = ProfileIcon("12345678901", 152.0)

    val micIcon = MaterialIconView(MaterialIcon.MIC_NONE, "25px")
    val rightArrow = MaterialIconView(MaterialIcon.ARROW_FORWARD, "25px")



   override val root = hbox {
        spacing = 48.0
        alignment= Pos.CENTER
        vbox {
            micIcon.fill = c("#CC4141")
            spacing = 12.0
            alignment= Pos.CENTER

            stackpane {
                hide()
                audioListened.subscribeBy (
                        onNext= { if(it) show() else hide() }
                )
                circle {

                    style{
                        radius= 55.0
                        fill = c("#E5E5E5")
                    }
                }

                button("",micIcon) {



                    importStylesheet(ButtonStyles::class)
                    addClass(ButtonStyles.roundButton)
                    style {

                        backgroundColor += Color.WHITE
                        cursor = Cursor.HAND
                        minWidth = 75.0.px
                        minHeight = 75.0.px
                        fontSize = 2.em
                        textFill = c("#CC4141")
                    }
                    action{
                        viewModel.listenedAudio(false)
                        find(ProfilePreview::class).replaceWith(UserCreation::class)

                    }
                }
            }

            label("REDO") {
                hide()
                audioListened.subscribeBy (
                        onNext= { if(it) show() else hide() }
                )
            }
        }

        stackpane {
            circle {

                style{
                    radius= 120.0
                    fill = c("#E5E5E5")
                }

            }
            iconHash.subscribeBy (
                onNext ={ add(NewUserButton)
                          NewUserButton.svgHash = it
                }
            )


            NewUserButton.profIcon.action {
                viewModel.listenedAudio(true)
            }
        }


        vbox {
            spacing = 12.0
            alignment= Pos.CENTER
            rightArrow.fill = c("#FFFFFF")
            stackpane {
                hide()
                audioListened.subscribeBy (
                        onNext= {  if(it) show() else hide() }
                )
                circle {

                    style{
                        radius= 55.0
                        fill = c("#E5E5E5")
                    }

                }
                button("", rightArrow) {


                    importStylesheet(ButtonStyles::class)
                    addClass(ButtonStyles.roundButton)
                    style {
                        backgroundColor += c("#CC4141")
                        cursor = Cursor.HAND
                        minWidth = 75.0.px
                        minHeight = 75.0.px
                        fontSize = 2.em
                        textFill = c("#CC4141")
                    }

                    action{

                    }
                }


            }

            label("NEXT"){
                hide()
                audioListened.subscribeBy (
                        onNext= {
                            if(it) show() else hide()
                             }
                )
            }
        }

    }


    init{
        viewModel.newIconHash("12345678901")
    }



}