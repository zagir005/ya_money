package com.zagirlek.finance.api.category

import com.zagirlek.finance.api.transaction.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoriesRepository {
    fun observeCategories(type: TransactionType? = null): Flow<List<Category>>

    suspend fun refreshCategories()
}
