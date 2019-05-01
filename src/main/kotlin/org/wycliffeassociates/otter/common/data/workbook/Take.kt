package org.wycliffeassociates.otter.common.data.workbook

import com.jakewharton.rxrelay2.BehaviorRelay
import org.wycliffeassociates.otter.common.data.model.MimeType
import java.io.File
import java.time.LocalDate

data class Take(
    val name: String,
    val file: File,
    val number: Int,
    val format: MimeType,
    val createdTimestamp: LocalDate,
    val deletedTimestamp: BehaviorRelay<DateHolder>
)

data class DateHolder(val value: LocalDate?)
