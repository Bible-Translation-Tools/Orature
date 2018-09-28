package org.wycliffeassociates.otter.common.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single

interface IRepository<T> {
    fun insert(obj: T): Completable
    fun getAll(): Single<List<T>>
    fun update(obj: T): Completable
    fun delete(obj: T): Completable
}