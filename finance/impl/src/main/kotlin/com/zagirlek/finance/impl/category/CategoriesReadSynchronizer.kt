package com.zagirlek.finance.impl.category

import com.zagirlek.finance.impl.category.remote.CategoriesRemoteDataSource
import com.zagirlek.finance.impl.category.remote.toEntity
import com.zagirlek.finance.impl.local.FinanceLocalTransactionRunner
import com.zagirlek.finance.impl.local.category.CategoriesLocalDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class CategoriesReadSynchronizer(
    private val localDataSource: CategoriesLocalDataSource,
    private val remoteDataSource: CategoriesRemoteDataSource,
    private val transactionRunner: FinanceLocalTransactionRunner,
) {
    private val refreshMutex = Mutex()

    suspend fun refresh() = refreshMutex.withLock {
        val remoteCategories = remoteDataSource.getCategories()

        transactionRunner.run {
            localDataSource.upsertEntities(remoteCategories.map { it.toEntity() })
        }
    }
}
