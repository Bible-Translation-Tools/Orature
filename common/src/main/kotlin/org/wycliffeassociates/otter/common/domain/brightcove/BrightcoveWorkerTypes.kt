/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.brightcove

import com.fasterxml.jackson.annotation.JsonProperty

data class BrightcoveWorkerAuthStartResponse(
    @JsonProperty("state")
    val state: String,
    @JsonProperty("login_url")
    val loginUrl: String,
    @JsonProperty("poll_url")
    val pollUrl: String,
    @JsonProperty("expires_in_seconds")
    val expiresInSeconds: Long?
)

data class BrightcoveWorkerAuthPollResponse(
    @JsonProperty("status")
    val status: String,
    @JsonProperty("token")
    val token: String? = null,
    @JsonProperty("expires_at_epoch_seconds")
    val expiresAtEpochSeconds: Long? = null
)

data class BrightcoveWorkerUploadResult(
    @JsonProperty("key")
    val key: String
)

data class BrightcoveWorkerIngestRequest(
    @JsonProperty("upload_id")
    val uploadId: String,
    @JsonProperty("audio_key")
    val audioKey: String,
    @JsonProperty("vtt_key")
    val vttKey: String? = null,
    @JsonProperty("video")
    val video: BrightcoveVideoRequest,
    @JsonProperty("text_track")
    val textTrack: BrightcoveWorkerTextTrack? = null,
    @JsonProperty("ingest_profile")
    val ingestProfile: String? = null,
    @JsonProperty("callbacks")
    val callbacks: List<String>? = null
)

data class BrightcoveWorkerIngestResult(
    @JsonProperty("video_id")
    val videoId: String?,
    @JsonProperty("ingest_job_id")
    val ingestJobId: String?
)

data class BrightcoveWorkerTextTrack(
    @JsonProperty("srclang")
    val srclang: String,
    @JsonProperty("kind")
    val kind: String,
    @JsonProperty("label")
    val label: String,
    @JsonProperty("default")
    val isDefault: Boolean = true
)
