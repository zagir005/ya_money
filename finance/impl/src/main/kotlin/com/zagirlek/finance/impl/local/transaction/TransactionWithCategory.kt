package com.zagirlek.finance.impl.local.transaction

import androidx.room.Embedded
import androidx.room.Relation
import com.zagirlek.finance.impl.local.category.CategoryEntity

internal data class TransactionWithCategory(
    @Embedded
    val transaction: TransactionEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id",
    )
    val category: CategoryEntity,
)
