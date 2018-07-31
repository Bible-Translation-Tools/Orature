package app.ui.welcomeScreen

import data.model.User
import data.persistence.AppPreferences
import tornadofx.ViewModel


class WelcomeScreenVM(var user: User) : ViewModel() {
//    val id = bind { user.idProperty }
//    val audioHash = bind { user.audioHashProperty }
//    var imagePathProperty = bind<String> { user.imagePath }
//    var appPreferences: AppPreferences? = null
    val appPreferences = DaggerPreferencesComponent
//    init {
//        appPreferences?.getCurrentUserId()
//    }

}
