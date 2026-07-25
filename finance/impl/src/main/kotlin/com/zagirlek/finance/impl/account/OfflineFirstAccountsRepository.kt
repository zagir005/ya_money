package com.zagirlek.finance.impl.account

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.impl.local.account.AccountsLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class OfflineFirstAccountsRepository(
    private val localDataSource: AccountsLocalDataSource,
    private val readSynchronizer: AccountsReadSynchronizer,
) : AccountsRepository {
    override fun observeAccounts(): Flow<List<Account>> =
        localDataSource.observeAccounts()

    override fun observeAccount(id: AccountId): Flow<Account?> =
        localDataSource.observeAccount(id)

    override suspend fun refreshAccounts() =
        readSynchronizer.refresh()

    @Deprecated(
        message = "Screen components must observe Room-backed data through observeAccounts().",
    )
    override suspend fun getAccounts(): List<Account> {
        val cached = observeAccounts().first()
        return try {
            refreshAccounts()
            observeAccounts().first()
        } catch (error: Exception) {
            if (cached.isNotEmpty()) cached else throw error
        }
    }
}
