package com.zagirlek.finance.impl.local.category

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
internal data class CategoryEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val emoji: String,
    @ColumnInfo(name = "is_income")
    val isIncome: Boolean,
)
