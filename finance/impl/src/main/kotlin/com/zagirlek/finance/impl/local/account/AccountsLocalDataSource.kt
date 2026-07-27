package com.zagirlek.finance.impl.local.account

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.impl.local.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class AccountsLocalDataSource(
    private val accountDao: AccountDao,
) {
    fun observeAccounts(): Flow<List<Account>> =
        accountDao.observeAll().map { accounts -> accounts.map(AccountEntity::toDomain) }

    fun observeAccount(accountId: AccountId): Flow<Account?> =
        accountDao.observeByClientId(accountId.value).map { it?.toDomain() }

    suspend fun getEntity(accountId: AccountId): AccountEntity? =
        accountDao.getByClientId(accountId.value)

    suspend fun getEntity(remoteId: Long): AccountEntity? =
        accountDao.getByRemoteId(remoteId)

    suspend fun getRemoteBackedEntities(): List<AccountEntity> =
        accountDao.getAllWithRemoteId()

    suspend fun upsert(account: AccountEntity) = accountDao.upsert(account)

    suspend fun upsertAll(accounts: List<AccountEntity>) = accountDao.upsertAll(accounts)

    suspend fun updateSyncStatus(
        accountId: AccountId,
        syncStatus: SyncStatus,
        updatedAtLocalMillis: Long,
    ) = accountDao.updateSyncStatus(
        clientId = accountId.value,
        syncStatus = syncStatus,
        updatedAtLocalMillis = updatedAtLocalMillis,
    )
}
