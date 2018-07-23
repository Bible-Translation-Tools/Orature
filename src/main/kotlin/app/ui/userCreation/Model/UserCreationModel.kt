package app.ui.userCreation.Model

import java.awt.Image

class UserCreationModel {

    var isRecording = false
    lateinit var audioFile: String
    lateinit var UserIcon: Image
    var hasListened = false



    fun recordClicked() {
        if(isRecording == false) isRecording=true
        else isRecording = false
    }

    fun reset() {
        isRecording = false
        hasListened = false
    }

}