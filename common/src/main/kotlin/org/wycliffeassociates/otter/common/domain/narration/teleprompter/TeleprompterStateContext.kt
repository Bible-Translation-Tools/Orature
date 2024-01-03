package org.wycliffeassociates.otter.common.domain.narration.teleprompter

class TeleprompterStateContext {
    lateinit var state: TeleprompterState
        internal set

    private var temporarilyDisabledState: TeleprompterState? = null

    fun changeState(request: TeleprompterItemState) {
        temporarilyDisabledState = null
        state = state.changeState(request)
    }

    fun disable() {
        if (temporarilyDisabledState == null) {
            temporarilyDisabledState = state
        }
        state = state.disabledState
    }

    fun restore() {
        state = temporarilyDisabledState ?: state
    }
}
