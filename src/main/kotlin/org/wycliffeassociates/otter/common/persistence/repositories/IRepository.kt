package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single

interface IRepository<T> {
    fun insert(obj: T): Single<Int>
    fun getAll(): Single<List<T>>
    fun update(obj: T): Completable
    fun delete(obj: T): Completable
}