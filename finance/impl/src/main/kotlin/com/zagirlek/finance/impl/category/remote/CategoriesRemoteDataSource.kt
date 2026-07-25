package com.zagirlek.finance.impl.category.remote

import com.zagirlek.finance.impl.network.FinanceHttpClient
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class CategoriesRemoteDataSource(
    private val httpClient: FinanceHttpClient,
    private val ioContext: CoroutineContext = Dispatchers.IO,
) {
    suspend fun getCategories(): List<CategoryDto> = withContext(ioContext) {
        httpClient.get(path = "categories")
    }
}
