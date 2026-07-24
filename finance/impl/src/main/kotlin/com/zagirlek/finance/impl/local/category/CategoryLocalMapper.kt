package com.zagirlek.finance.impl.local.category

import com.zagirlek.finance.api.category.Category
import com.zagirlek.finance.api.category.CategoryId
import com.zagirlek.finance.api.transaction.TransactionType

internal fun CategoryEntity.toDomain(): Category = Category(
    id = CategoryId(id),
    name = name,
    emoji = emoji,
    type = if (isIncome) TransactionType.Income else TransactionType.Expense,
)

internal fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id.value,
    name = name,
    emoji = emoji,
    isIncome = type == TransactionType.Income,
)
