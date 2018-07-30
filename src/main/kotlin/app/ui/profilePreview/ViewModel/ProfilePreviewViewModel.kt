package app.ui.profilePreview.ViewModel

import app.ui.profilePreview.Model.ProfilePreviewModel
import io.reactivex.subjects.PublishSubject

class ProfilePreviewViewModel(
        private val userIconHash: PublishSubject<String>,
        private val clickNext: PublishSubject<Boolean>,
        private val clickRedo: PublishSubject<Boolean>,
        private val userListenedAudio: PublishSubject<Boolean>) {
    private val model = ProfilePreviewModel()
    fun newIconHash(iconHash: String) {
        userIconHash.onNext(iconHash)
        model.userIconHash = iconHash
    }

    fun clickRedo(clicked: Boolean) {
        clickRedo.onNext(clicked)
        model.clickRedo = clicked
    }

    fun clickNext(clicked: Boolean) {
        clickNext.onNext(clicked)
        model.clickNext = clicked
    }

    fun listenedAudio(listened: Boolean) {
        userListenedAudio.onNext(listened)
        model.listenedAudio = listened
    }
}