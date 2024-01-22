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
package org.wycliffeassociates.otter.common.data.workbook

import com.jakewharton.rxrelay2.BehaviorRelay
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.utils.computeFileChecksum
import java.io.File
import java.time.LocalDate

class Take(
    val name: String,
    val file: File,
    val number: Int,
    val format: MimeType,
    val createdTimestamp: LocalDate,
    val deletedTimestamp: BehaviorRelay<DateHolder> = BehaviorRelay.createDefault(DateHolder.empty),
    val checkingState: BehaviorRelay<TakeCheckingState> = BehaviorRelay.createDefault(TakeCheckingState(CheckingStatus.UNCHECKED, null))
) {
    fun checksum() = computeFileChecksum(file)

    fun getSavedChecksum() = checkingState.value?.checksum

    override fun equals(other: Any?): Boolean {
        return (other as? Take)?.let {
            it.file == this.file
        } ?: false
    }

    override fun hashCode() = file.hashCode()

    fun isDeleted() = deletedTimestamp.value?.value != null
}

data class DateHolder(val value: LocalDate?) {
    companion object {
        val empty = DateHolder(null)
        fun now() = DateHolder(LocalDate.now())
    }
}
