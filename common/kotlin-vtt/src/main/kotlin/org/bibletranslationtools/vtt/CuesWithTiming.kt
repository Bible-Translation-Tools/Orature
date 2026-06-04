package org.bibletranslationtools.vtt

/** A list of [Cue] instances with a start time and duration.  */
class CuesWithTiming(
    cues: List<Cue>,
    /**
     * The time at which [.cues] should be shown on screen, in microseconds, or [ ][C.TIME_UNSET] if not known.
     *
     *
     * The time base of this depends on the context from which this instance was obtained.
     */
    val startTimeUs: Long,
    /**
     * The duration for which [.cues] should be shown on screen, in microseconds, or [ ][C.TIME_UNSET] if not known.
     *
     *
     * If [Format.cueReplacementBehavior] is [Format.CUE_REPLACEMENT_BEHAVIOR_MERGE]
     * then cues from multiple instances will be shown on screen simultaneously if their start times
     * and durations overlap.
     *
     *
     * [C.TIME_UNSET] is only permitted if the [Format.cueReplacementBehavior] of the
     * current track is [Format.CUE_REPLACEMENT_BEHAVIOR_REPLACE].
     */
    val durationUs: Long
) {
    /** The cues to show on screen.  */
    val cues: List<Cue> = cues.map { it }

    /**
     * The time at which [.cues] should stop being shown on screen, in microseconds, or [ ][C.TIME_UNSET] if not known.
     *
     *
     * The time base of this is the same as [.startTimeUs].
     *
     *
     * If [Format.cueReplacementBehavior] is [Format.CUE_REPLACEMENT_BEHAVIOR_MERGE]
     * then cues from multiple instances will be shown on screen simultaneously if their start and
     * times overlap.
     *
     *
     * [C.TIME_UNSET] is only permitted if the [Format.cueReplacementBehavior] of the
     * current track is [Format.CUE_REPLACEMENT_BEHAVIOR_REPLACE].
     */
    val endTimeUs: Long = startTimeUs + durationUs
}