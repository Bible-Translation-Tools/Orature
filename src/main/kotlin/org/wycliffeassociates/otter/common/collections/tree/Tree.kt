package org.wycliffeassociates.otter.common.collections.tree

class Tree(value: Any) : TreeNode(value) {

    //private mutable list, public immutable accessor
    private val _children = arrayListOf<TreeNode>()
    val children: List<TreeNode> = _children

    fun addChild(node: TreeNode) {
        _children.add(node)
    }

    fun addAll(nodes: Array<TreeNode>) {
        _children.addAll(nodes)
    }

    fun addAll(nodes: Collection<TreeNode>) {
        _children.addAll(nodes)
    }
}

open class TreeNode(val value: Any)