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

import io.reactivex.Maybe
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

interface BrightcoveClient {
    fun findVideoIdByReferenceId(config: BrightcoveConfig, referenceId: String): Maybe<String>

    fun createVideo(config: BrightcoveConfig, request: BrightcoveVideoRequest): Single<String>

    fun uploadSource(config: BrightcoveConfig, videoId: String, sourceFile: File): Single<BrightcoveUploadResult>

    fun updateCuePoints(
        config: BrightcoveConfig,
        videoId: String,
        cuePoints: List<BrightcoveCuePoint>
    ): Completable

    fun ingest(
        config: BrightcoveConfig,
        videoId: String,
        masterUrl: String,
        textTracks: List<BrightcoveTextTrack> = emptyList()
    ): Single<BrightcoveIngestResult>
}
