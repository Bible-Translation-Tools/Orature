package app.ui.profilePreview.ViewModel

import app.ui.profilePreview.Model.ProfilePreviewModel
import io.reactivex.subjects.PublishSubject
import tornadofx.ViewModel

class ProfilePreviewViewModel: ViewModel() {
    private val model = ProfilePreviewModel()
    var userIconHash = PublishSubject.create<String>()                // subject to get the user iconHash
    var onClickNext = PublishSubject.create<Boolean>()          // subject to check if the NEXT button was clicked
    var onClickRedo = PublishSubject.create<Boolean>()             // subject to check if the REDO button was clicked
    var audioListened = PublishSubject.create<Boolean>()            // subject to check if the audio was listened


    fun newIconHash(iconHash: String) {
        userIconHash.onNext(iconHash)
        model.userIconHash = iconHash
    }

    fun clickRedo(clicked: Boolean) {
        onClickRedo.onNext(clicked)
        model.clickRedo = clicked
    }

    fun clickNext(clicked: Boolean) {
        onClickNext.onNext(clicked)
        model.clickNext = clicked
    }

    fun hasBeenPlayed(listened: Boolean) {
        audioListened.onNext(listened)
        model.listenedAudio = listened
    }
}