package com.zagirlek.finance.impl.local.category

import com.zagirlek.finance.api.category.Category
import com.zagirlek.finance.api.category.CategoryId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class CategoriesLocalDataSource(
    private val categoryDao: CategoryDao,
) {
    fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { categories -> categories.map(CategoryEntity::toDomain) }

    suspend fun getCategory(categoryId: CategoryId): Category? =
        categoryDao.getById(categoryId.value)?.toDomain()

    suspend fun upsertAll(categories: List<Category>) =
        categoryDao.upsertAll(categories.map(Category::toEntity))

    suspend fun upsertEntities(categories: List<CategoryEntity>) =
        categoryDao.upsertAll(categories)
}
