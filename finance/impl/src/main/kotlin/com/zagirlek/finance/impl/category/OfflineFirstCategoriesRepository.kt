package com.zagirlek.finance.impl.category

import com.zagirlek.finance.api.category.CategoriesRepository
import com.zagirlek.finance.api.category.Category
import com.zagirlek.finance.api.transaction.TransactionType
import com.zagirlek.finance.impl.local.category.CategoriesLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OfflineFirstCategoriesRepository(
    private val localDataSource: CategoriesLocalDataSource,
    private val readSynchronizer: CategoriesReadSynchronizer,
) : CategoriesRepository {
    override fun observeCategories(type: TransactionType?): Flow<List<Category>> =
        localDataSource.observeCategories().map { categories ->
            if (type == null) categories else categories.filter { it.type == type }
        }

    override suspend fun refreshCategories() =
        readSynchronizer.refresh()
}
