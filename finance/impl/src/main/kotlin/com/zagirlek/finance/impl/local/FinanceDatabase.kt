package com.zagirlek.finance.impl.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zagirlek.finance.impl.local.account.AccountDao
import com.zagirlek.finance.impl.local.account.AccountEntity
import com.zagirlek.finance.impl.local.category.CategoryDao
import com.zagirlek.finance.impl.local.category.CategoryEntity
import com.zagirlek.finance.impl.local.sync.PendingOperationDao
import com.zagirlek.finance.impl.local.sync.PendingOperationEntity
import com.zagirlek.finance.impl.local.sync.SyncWindowDao
import com.zagirlek.finance.impl.local.sync.SyncWindowEntity
import com.zagirlek.finance.impl.local.transaction.TransactionDao
import com.zagirlek.finance.impl.local.transaction.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        PendingOperationEntity::class,
        SyncWindowEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(FinanceTypeConverters::class)
internal abstract class FinanceDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao

    abstract fun categoryDao(): CategoryDao

    abstract fun transactionDao(): TransactionDao

    abstract fun pendingOperationDao(): PendingOperationDao

    abstract fun syncWindowDao(): SyncWindowDao
}

internal object FinanceDatabaseFactory {
    fun create(context: Context): FinanceDatabase =
        Room.databaseBuilder(
            context = context.applicationContext,
            klass = FinanceDatabase::class.java,
            name = DATABASE_NAME,
        ).build()

    private const val DATABASE_NAME = "finance.db"
}
