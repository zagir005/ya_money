package com.zagirlek.finance.impl.account.remote

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.impl.network.FinanceHttpClient
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class RemoteAccountsRepository(
    private val httpClient: FinanceHttpClient,
    private val ioContext: CoroutineContext = Dispatchers.IO,
) : AccountsRepository {

    override fun observeAccounts(): Flow<List<Account>> = flow {
        emit(loadAccounts())
    }

    override suspend fun refreshAccounts() {
        loadAccounts()
    }

    override suspend fun getAccounts(): List<Account> = loadAccounts()

    private suspend fun loadAccounts(): List<Account> = withContext(ioContext) {
        httpClient.get<List<AccountDto>>(path = ACCOUNTS_PATH).map(AccountDto::toDomain)
    }

    private companion object {
        const val ACCOUNTS_PATH = "accounts"
    }
}
