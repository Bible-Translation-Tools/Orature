package org.wycliffeassociates.otter.common.domain.repositories

import io.reactivex.Completable

interface IRepository<T> {
    fun insert(obj: T): Completable
    fun update(obj: T): Completable
    fun delete(obj: T): Completable
}