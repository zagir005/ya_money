package com.zagirlek.finance.impl

import android.content.Context
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.category.CategoriesRepository
import com.zagirlek.finance.api.sync.FinanceSyncStatusRepository
import com.zagirlek.finance.api.transaction.TransactionsRepository
import com.zagirlek.finance.impl.account.AccountsReadSynchronizer
import com.zagirlek.finance.impl.account.OfflineFirstAccountsRepository
import com.zagirlek.finance.impl.account.RoomAccountsCommandHandler
import com.zagirlek.finance.impl.account.remote.AccountsRemoteDataSource
import com.zagirlek.finance.impl.category.CategoriesReadSynchronizer
import com.zagirlek.finance.impl.category.OfflineFirstCategoriesRepository
import com.zagirlek.finance.impl.category.remote.CategoriesRemoteDataSource
import com.zagirlek.finance.impl.local.FinanceDatabaseFactory
import com.zagirlek.finance.impl.local.FinanceLocalTransactionRunner
import com.zagirlek.finance.impl.local.account.AccountsLocalDataSource
import com.zagirlek.finance.impl.local.category.CategoriesLocalDataSource
import com.zagirlek.finance.impl.local.sync.SyncLocalDataSource
import com.zagirlek.finance.impl.local.transaction.TransactionsLocalDataSource
import com.zagirlek.finance.impl.network.FinanceHttpClient
import com.zagirlek.finance.impl.transaction.OfflineFirstTransactionsRepository
import com.zagirlek.finance.impl.transaction.RoomTransactionsCommandHandler
import com.zagirlek.finance.impl.transaction.TransactionsReadSynchronizer
import com.zagirlek.finance.impl.transaction.remote.TransactionsRemoteDataSource
import com.zagirlek.finance.impl.sync.OutboxDelivery
import com.zagirlek.finance.impl.sync.RoomFinanceSyncStatusRepository
import java.io.Closeable
import java.time.Clock
import kotlinx.serialization.json.Json

class FinanceDataGraph(
    context: Context,
    httpClient: FinanceHttpClient,
    clock: Clock = Clock.systemDefaultZone(),
    onSyncRequested: () -> Unit = {},
) : Closeable {
    private val database = FinanceDatabaseFactory.create(context)
    private val transactionRunner = FinanceLocalTransactionRunner(database)

    private val accountsLocalDataSource = AccountsLocalDataSource(database.accountDao())
    private val categoriesLocalDataSource = CategoriesLocalDataSource(database.categoryDao())
    private val transactionsLocalDataSource = TransactionsLocalDataSource(database.transactionDao())
    private val syncLocalDataSource = SyncLocalDataSource(
        pendingOperationDao = database.pendingOperationDao(),
        syncWindowDao = database.syncWindowDao(),
    )
    private val accountsRemoteDataSource = AccountsRemoteDataSource(httpClient)
    private val categoriesRemoteDataSource = CategoriesRemoteDataSource(httpClient)
    private val transactionsRemoteDataSource = TransactionsRemoteDataSource(httpClient)

    private val accountsSynchronizer = AccountsReadSynchronizer(
        localDataSource = accountsLocalDataSource,
        transactionsLocalDataSource = transactionsLocalDataSource,
        remoteDataSource = accountsRemoteDataSource,
        transactionRunner = transactionRunner,
        clock = clock,
    )
    private val categoriesSynchronizer = CategoriesReadSynchronizer(
        localDataSource = categoriesLocalDataSource,
        remoteDataSource = categoriesRemoteDataSource,
        transactionRunner = transactionRunner,
    )
    private val transactionsSynchronizer = TransactionsReadSynchronizer(
        accountsSynchronizer = accountsSynchronizer,
        categoriesSynchronizer = categoriesSynchronizer,
        accountsLocalDataSource = accountsLocalDataSource,
        categoriesLocalDataSource = categoriesLocalDataSource,
        transactionsLocalDataSource = transactionsLocalDataSource,
        syncLocalDataSource = syncLocalDataSource,
        remoteDataSource = transactionsRemoteDataSource,
        transactionRunner = transactionRunner,
        clock = clock,
    )
    private val transactionsCommandHandler = RoomTransactionsCommandHandler(
        accountsLocalDataSource = accountsLocalDataSource,
        categoriesLocalDataSource = categoriesLocalDataSource,
        transactionsLocalDataSource = transactionsLocalDataSource,
        syncLocalDataSource = syncLocalDataSource,
        transactionRunner = transactionRunner,
        clock = clock,
        json = Json,
    )
    private val accountsCommandHandler = RoomAccountsCommandHandler(
        localDataSource = accountsLocalDataSource,
        syncLocalDataSource = syncLocalDataSource,
        transactionRunner = transactionRunner,
        clock = clock,
        json = Json,
    )
    private val outboxDelivery = OutboxDelivery(
        accountsLocalDataSource = accountsLocalDataSource,
        transactionsLocalDataSource = transactionsLocalDataSource,
        categoriesLocalDataSource = categoriesLocalDataSource,
        syncLocalDataSource = syncLocalDataSource,
        accountsRemoteDataSource = accountsRemoteDataSource,
        transactionsRemoteDataSource = transactionsRemoteDataSource,
        transactionRunner = transactionRunner,
        clock = clock,
        json = Json,
    )
    val syncCoordinator = FinanceSyncCoordinator(
        outboxDelivery = outboxDelivery,
        accountsSynchronizer = accountsSynchronizer,
        categoriesSynchronizer = categoriesSynchronizer,
        transactionsSynchronizer = transactionsSynchronizer,
        syncLocalDataSource = syncLocalDataSource,
    )
    val syncStatusRepository: FinanceSyncStatusRepository =
        RoomFinanceSyncStatusRepository(
            accountsLocalDataSource = accountsLocalDataSource,
            transactionsLocalDataSource = transactionsLocalDataSource,
            syncLocalDataSource = syncLocalDataSource,
            transactionRunner = transactionRunner,
            clock = clock,
            onSyncRequested = onSyncRequested,
        )

    val accountsRepository: AccountsRepository = OfflineFirstAccountsRepository(
        localDataSource = accountsLocalDataSource,
        syncCoordinator = syncCoordinator,
        commandHandler = accountsCommandHandler,
        onSyncRequested = onSyncRequested,
    )
    val categoriesRepository: CategoriesRepository = OfflineFirstCategoriesRepository(
        localDataSource = categoriesLocalDataSource,
        readSynchronizer = categoriesSynchronizer,
    )
    val transactionsRepository: TransactionsRepository = OfflineFirstTransactionsRepository(
        localDataSource = transactionsLocalDataSource,
        syncCoordinator = syncCoordinator,
        commandHandler = transactionsCommandHandler,
        onSyncRequested = onSyncRequested,
    )

    override fun close() {
        database.close()
    }
}
