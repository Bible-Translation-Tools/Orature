/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.utils

import javafx.scene.Node
import javafx.scene.Parent
import kotlin.reflect.KClass

inline fun <reified T: Node> Node.findChild(): Node? = findChildren<T>().firstOrNull()
inline fun <reified T: Node> Node.findChildren(): List<Node> = findChildren(T::class)
fun <T: Node> Node.findChildren(type: KClass<T>): List<Node> {
    if (this !is Parent) return listOf()

    val list = this.childrenUnmodifiable
        .filter { type.isInstance(it) }
        .filter { it.isVisible }
        .toMutableList()

    for (node: Node in this.childrenUnmodifiable) {
        (node as? Parent)?.findChildren(type)?.let {
            list.addAll(it)
        }
    }

    return list
}
