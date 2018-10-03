package org.wycliffeassociates.otter.jvm.persistence.database.daos

interface IDao<E> {
    fun insert(entity: E): Int
    fun fetchById(id: Int): E
    fun fetchAll(): List<E>
    fun update(entity: E)
    fun delete(entity: E)
}