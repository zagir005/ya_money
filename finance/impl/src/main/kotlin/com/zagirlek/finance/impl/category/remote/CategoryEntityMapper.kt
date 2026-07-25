package com.zagirlek.finance.impl.category.remote

import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.impl.local.category.CategoryEntity

internal fun CategoryDto.toEntity(): CategoryEntity = try {
    require(id > 0)
    require(name.isNotBlank())
    require(emoji.isNotBlank())
    CategoryEntity(
        id = id,
        name = name,
        emoji = emoji,
        isIncome = isIncome,
    )
} catch (error: RuntimeException) {
    throw FinanceNetworkException.InvalidResponse(error)
}
