
import javafx.beans.property.SimpleStringProperty
import java.util.*

object UIColorsManager {
    val colorResourceFile = SimpleStringProperty("UIColors")
    var Colors = ResourceBundle.getBundle(colorResourceFile.value)
}