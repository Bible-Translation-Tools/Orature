package app.ui

import javafx.scene.layout.VBox
import tornadofx.*

object Application {
    class MyApp : App(MyView::class, ChartCSS::class) {
        // starts App in maximized window
        /*override fun start(stage: Stage) {
            super.start(stage)
            stage.isMaximized = true
        }*/

        // loads the stylesheet
        init {
            reloadStylesheetsOnFocus()
        }
    }

    /**
     * The main view for the UI. (Think React-style.)
     */
    class MyView : View() {
        override val root = VBox()
        init {
            with(root) {
                // root.nodeOrientation = NodeOrientation.RIGHT_TO_LEFT

                // pull some views
                val mainView = find(ProgressBar::class)
                val buttonAttempt = find(languageSelector::class)
                val carlsChipView = find(TagCloudView::class)

                // stick those bois in there
                borderpane {
                    center = mainView.root
                    left = buttonAttempt.root
                    // right = carlsChipView.root
                    // left = leftSideBar.root
                    // right = leftSideBar.root
                    // top = titleBanner.root
                }

                // sets background color for the whole app
                style {
                    //backgroundColor += Color.ANTIQUEWHITE
                    padding = box(5.px, 10.px, 10.px, 10.px)
                }
            }
        }
    }
}

/**
 * Launches the app!
 */
fun main(args: Array<String>) {
    // set the locale
    //Locale.setDefault(Locale.forLanguageTag("fr"))
    launch<Application.MyApp>(args)
}