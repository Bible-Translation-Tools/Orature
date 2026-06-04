package org.bibletranslationtools.vtt

interface Subtitle {
    /**
     * Returns the index of the first event that occurs after a given time (exclusive).
     *
     * @param timeUs The time in microseconds.
     * @return The index of the next event, or [C.INDEX_UNSET] if there are no events after the
     * specified time.
     */
    fun getNextEventTimeIndex(timeUs: Long): Int

    /**
     * Returns the number of event times, where events are defined as points in time at which the cues
     * returned by [.getCues] changes.
     *
     * @return The number of event times.
     */
    fun getEventTimeCount(): Int

    /**
     * Returns the event time at a specified index.
     *
     * @param index The index of the event time to obtain.
     * @return The event time in microseconds.
     */
    fun getEventTime(index: Int): Long

    /**
     * Retrieve the cues that should be displayed at a given time.
     *
     * @param timeUs The time in microseconds.
     * @return A list of cues that should be displayed, possibly empty.
     */
    fun getCues(timeUs: Long): List<Cue>
}