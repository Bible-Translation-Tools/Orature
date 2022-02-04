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
package org.wycliffeassociates.otter.common.collections

class OtterTree<T>(value: T) : OtterTreeNode<T>(value) {

    // private mutable list, public immutable accessor
    private val _children = arrayListOf<OtterTreeNode<T>>()
    val children: List<OtterTreeNode<T>> = _children

    fun addChild(node: OtterTreeNode<T>) {
        _children.add(node)
    }

    fun addAll(nodes: Array<OtterTreeNode<T>>) {
        _children.addAll(nodes)
    }

    fun addAll(nodes: Collection<OtterTreeNode<T>>) {
        _children.addAll(nodes)
    }

    override fun <U> map(f: (T) -> U): OtterTree<U> =
        OtterTree(f(value))
            .also { treeU ->
                treeU.addAll(
                    this.children.map { it.map(f) }
                )
            }

    override fun filter(f: (T) -> Boolean): OtterTree<T>? =
        if (f(value)) {
            OtterTree(value)
                .also { newRoot ->
                    newRoot.addAll(
                        this.children.mapNotNull { it.filter(f) }
                    )
                }
        } else {
            null
        }

    override fun filterPreserveParents(f: (T) -> Boolean): OtterTree<T>? {
        val prunedChildren = this.children.mapNotNull { it.filterPreserveParents(f) }
        return if (prunedChildren.isNotEmpty()) {
            OtterTree(value).apply { addAll(prunedChildren) }
        } else if (f(value)) {
            OtterTree(value)
        } else {
            null
        }
    }
}

open class OtterTreeNode<out T>(val value: T) {
    open fun <U> map(f: (T) -> U): OtterTreeNode<U> = OtterTreeNode(f(value))
    open fun filter(f: (T) -> Boolean): OtterTreeNode<T>? = if (f(value)) this else null
    open fun filterPreserveParents(f: (T) -> Boolean): OtterTreeNode<T>? = filter(f)
}
