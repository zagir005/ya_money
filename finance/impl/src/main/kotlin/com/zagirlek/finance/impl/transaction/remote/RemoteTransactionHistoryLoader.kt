package com.zagirlek.finance.impl.transaction.remote

import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.impl.network.FinanceHttpClient
import io.ktor.client.request.parameter
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RemoteTransactionHistoryLoader(
    private val accountsRepository: AccountsRepository,
    private val httpClient: FinanceHttpClient,
    private val ioContext: CoroutineContext = Dispatchers.IO,
) {
    suspend fun load(period: TransactionPeriod): List<TransactionResponseDto> = withContext(ioContext) {
        accountsRepository.getAccounts().flatMap { account ->
            httpClient.get<List<TransactionResponseDto>>(
                path = "transactions/account/${account.id.value}/period",
            ) {
                parameter("startDate", period.startDate.toString())
                parameter("endDate", period.endDate.toString())
            }
        }
    }
}
