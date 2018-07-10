import javafx.scene.image.Image
import javafx.scene.image.ImageView
import tornadofx.*;

//A button with the image in resources on it

class ButtonView : View() {
    override val root = gridpane(){
        val imagePlace = "Spring_backdrop.jpg";
        val imageMine = Image(imagePlace);
        val myButton = button {
            add(ImageView(imageMine));
        }

    }
}

class CSSView: View() {
    override val root = vbox {
        addClass(Styles.wrapper)

        label("Alice") {
            addClass(Styles.alice)
        }
        label("Bob") {
            addClass(Styles.bob)
        }
    }
}