package com.zagirlek.finance.impl.account

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.account.CreateAccount
import com.zagirlek.finance.api.account.UpdateAccount
import com.zagirlek.finance.impl.local.account.AccountsLocalDataSource
import com.zagirlek.finance.impl.FinanceSyncCoordinator
import kotlinx.coroutines.flow.Flow

internal class OfflineFirstAccountsRepository(
    private val localDataSource: AccountsLocalDataSource,
    private val syncCoordinator: FinanceSyncCoordinator,
    private val commandHandler: AccountsCommandHandler,
    private val onSyncRequested: () -> Unit,
) : AccountsRepository {
    override fun observeAccounts(): Flow<List<Account>> =
        localDataSource.observeAccounts()

    override fun observeAccount(id: AccountId): Flow<Account?> =
        localDataSource.observeAccount(id)

    override suspend fun refreshAccounts() {
        syncCoordinator.sync()
    }

    override suspend fun createAccount(command: CreateAccount): AccountId {
        val accountId = commandHandler.create(command)
        onSyncRequested()
        return accountId
    }

    override suspend fun updateAccount(command: UpdateAccount) {
        commandHandler.update(command)
        onSyncRequested()
    }
}
