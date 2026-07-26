package com.zagirlek.finance.impl.transaction.remote

import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.impl.network.FinanceHttpClient
import io.ktor.client.request.parameter
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class TransactionsRemoteDataSource(
    private val httpClient: FinanceHttpClient,
    private val ioContext: CoroutineContext = Dispatchers.IO,
) {
    suspend fun getTransactions(
        accountRemoteId: Long,
        period: TransactionPeriod,
    ): List<TransactionResponseDto> = withContext(ioContext) {
        httpClient.get(path = "transactions/account/$accountRemoteId/period") {
            parameter("startDate", period.startDate.toString())
            parameter("endDate", period.endDate.toString())
        }
    }

    suspend fun createTransaction(
        request: TransactionRequestDto,
    ): TransactionWriteResult = withContext(ioContext) {
        val response = httpClient.post<TransactionRequestDto, CreatedTransactionDto>(
            path = "transactions",
            body = request,
        )
        TransactionWriteResult(
            remoteId = response.id.toLong(),
            createdAt = response.createdAt,
            updatedAt = response.updatedAt,
        )
    }

    suspend fun updateTransaction(
        remoteId: Long,
        request: TransactionRequestDto,
    ): TransactionWriteResult = withContext(ioContext) {
        val response = httpClient.put<TransactionRequestDto, TransactionResponseDto>(
            path = "transactions/$remoteId",
            body = request,
        )
        TransactionWriteResult(
            remoteId = response.id.toLong(),
            createdAt = response.createdAt,
            updatedAt = response.updatedAt,
        )
    }
}
