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
package org.wycliffeassociates.otter.common.data.primitives

enum class Anthology(val titleKey: String) {
    OLD_TESTAMENT("oldTestament"),
    NEW_TESTAMENT("newTestament"),
    OTHER("")
}

val bookAnthology = mapOf(
    "gen" to Anthology.OLD_TESTAMENT,
    "exo" to Anthology.OLD_TESTAMENT,
    "lev" to Anthology.OLD_TESTAMENT,
    "num" to Anthology.OLD_TESTAMENT,
    "deu" to Anthology.OLD_TESTAMENT,
    "jos" to Anthology.OLD_TESTAMENT,
    "jdg" to Anthology.OLD_TESTAMENT,
    "rut" to Anthology.OLD_TESTAMENT,
    "1sa" to Anthology.OLD_TESTAMENT,
    "2sa" to Anthology.OLD_TESTAMENT,
    "1ki" to Anthology.OLD_TESTAMENT,
    "2ki" to Anthology.OLD_TESTAMENT,
    "1ch" to Anthology.OLD_TESTAMENT,
    "2ch" to Anthology.OLD_TESTAMENT,
    "ezr" to Anthology.OLD_TESTAMENT,
    "neh" to Anthology.OLD_TESTAMENT,
    "est" to Anthology.OLD_TESTAMENT,
    "job" to Anthology.OLD_TESTAMENT,
    "psa" to Anthology.OLD_TESTAMENT,
    "pro" to Anthology.OLD_TESTAMENT,
    "ecc" to Anthology.OLD_TESTAMENT,
    "sng" to Anthology.OLD_TESTAMENT,
    "isa" to Anthology.OLD_TESTAMENT,
    "jer" to Anthology.OLD_TESTAMENT,
    "lam" to Anthology.OLD_TESTAMENT,
    "ezk" to Anthology.OLD_TESTAMENT,
    "dan" to Anthology.OLD_TESTAMENT,
    "hos" to Anthology.OLD_TESTAMENT,
    "jol" to Anthology.OLD_TESTAMENT,
    "amo" to Anthology.OLD_TESTAMENT,
    "oba" to Anthology.OLD_TESTAMENT,
    "jon" to Anthology.OLD_TESTAMENT,
    "mic" to Anthology.OLD_TESTAMENT,
    "nam" to Anthology.OLD_TESTAMENT,
    "hab" to Anthology.OLD_TESTAMENT,
    "zep" to Anthology.OLD_TESTAMENT,
    "hag" to Anthology.OLD_TESTAMENT,
    "zec" to Anthology.OLD_TESTAMENT,
    "mal" to Anthology.OLD_TESTAMENT,
    "mat" to Anthology.NEW_TESTAMENT,
    "mrk" to Anthology.NEW_TESTAMENT,
    "luk" to Anthology.NEW_TESTAMENT,
    "jhn" to Anthology.NEW_TESTAMENT,
    "act" to Anthology.NEW_TESTAMENT,
    "rom" to Anthology.NEW_TESTAMENT,
    "1co" to Anthology.NEW_TESTAMENT,
    "2co" to Anthology.NEW_TESTAMENT,
    "gal" to Anthology.NEW_TESTAMENT,
    "eph" to Anthology.NEW_TESTAMENT,
    "php" to Anthology.NEW_TESTAMENT,
    "col" to Anthology.NEW_TESTAMENT,
    "1th" to Anthology.NEW_TESTAMENT,
    "2th" to Anthology.NEW_TESTAMENT,
    "1ti" to Anthology.NEW_TESTAMENT,
    "2ti" to Anthology.NEW_TESTAMENT,
    "tit" to Anthology.NEW_TESTAMENT,
    "phm" to Anthology.NEW_TESTAMENT,
    "heb" to Anthology.NEW_TESTAMENT,
    "jas" to Anthology.NEW_TESTAMENT,
    "1pe" to Anthology.NEW_TESTAMENT,
    "2pe" to Anthology.NEW_TESTAMENT,
    "1jn" to Anthology.NEW_TESTAMENT,
    "2jn" to Anthology.NEW_TESTAMENT,
    "3jn" to Anthology.NEW_TESTAMENT,
    "jud" to Anthology.NEW_TESTAMENT,
    "rev" to Anthology.NEW_TESTAMENT
)