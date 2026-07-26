package com.zagirlek.finance.impl.account.remote

import com.zagirlek.finance.impl.network.FinanceHttpClient
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AccountsRemoteDataSource(
    private val httpClient: FinanceHttpClient,
    private val ioContext: CoroutineContext = Dispatchers.IO,
) {
    suspend fun getAccounts(): List<AccountDto> = withContext(ioContext) {
        httpClient.get(path = "accounts")
    }

    suspend fun createAccount(request: AccountCreateRequestDto): AccountDto =
        withContext(ioContext) {
            httpClient.post(path = "accounts", body = request)
        }

    suspend fun updateAccount(
        remoteId: Long,
        request: AccountUpdateRequestDto,
    ): AccountDto = withContext(ioContext) {
        httpClient.put(path = "accounts/$remoteId", body = request)
    }
}
