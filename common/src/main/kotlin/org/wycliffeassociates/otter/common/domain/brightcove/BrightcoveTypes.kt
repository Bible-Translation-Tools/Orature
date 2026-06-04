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

data class BrightcoveVideoRequest(
    val name: String,
    val referenceId: String,
    val tags: List<String>,
    val customFields: Map<String, String>,
    val cuePoints: List<BrightcoveCuePoint> = emptyList()
)

data class CountryInfo(
    val code: String?,
    val name: String?
)

data class BrightcoveUploadResult(
    val masterUrl: String
)

data class BrightcoveIngestResult(
    val jobId: String?
)

data class BrightcoveTextTrack(
    @JsonProperty("url")
    val url: String,
    @JsonProperty("srclang")
    val srclang: String,
    @JsonProperty("kind")
    val kind: String,
    @JsonProperty("label")
    val label: String,
    @JsonProperty("default")
    val isDefault: Boolean = true
)

data class BrightcoveCuePoint(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("type")
    val type: String = "CODE",
    @JsonProperty("time")
    val time: Double,
    @JsonProperty("force_stop")
    val forceStop: Boolean = false
)

data class BrightcoveExportReport(
    val project: BrightcoveExportProject,
    val entries: List<BrightcoveExportEntry>
)

data class BrightcoveExportProject(
    val languageCode: String,
    val bookCode: String,
    val canonicalOrder: Int,
    val localizedBookName: String
)

data class BrightcoveExportEntry(
    val chapter: Int,
    val takeName: String?,
    val videoName: String,
    val videoId: String?,
    val vttFile: String?,
    val textTrackUrl: String?,
    val ingestJobId: String?,
    val status: String,
    val error: String?
)
