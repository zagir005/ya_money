package com.zagirlek.finance.impl.sync

import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.impl.local.PendingOperationType
import java.io.IOException
import java.math.BigDecimal
import java.net.ConnectException
import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncFailurePolicyTest {
    @Test
    fun `server failures retry twice and fail on third attempt`() {
        val error = FinanceNetworkException.ServerFailure(500)

        assertTrue(
            classifySyncFailure(PendingOperationType.Create, 0, error) is
                SyncFailureAction.RetryServerFailure,
        )
        assertTrue(
            classifySyncFailure(PendingOperationType.Create, 1, error) is
                SyncFailureAction.RetryServerFailure,
        )
        assertEquals(
            SyncFailureAction.MarkFailed,
            classifySyncFailure(PendingOperationType.Create, 2, error),
        )
    }

    @Test
    fun `create connection failure is safe to retry when request was not sent`() {
        val error = FinanceNetworkException.Network(ConnectException("offline"))

        assertTrue(
            classifySyncFailure(PendingOperationType.Create, 0, error) is
                SyncFailureAction.RetryLater,
        )
    }

    @Test
    fun `ambiguous create timeout is marked as unknown result`() {
        val error = FinanceNetworkException.Network(SocketTimeoutException("read timeout"))

        assertEquals(
            SyncFailureAction.MarkUnknownResult,
            classifySyncFailure(PendingOperationType.Create, 0, error),
        )
    }

    @Test
    fun `create conflict is reconciled before any repeat post`() {
        val error = FinanceNetworkException.ClientFailure(409)

        assertEquals(
            SyncFailureAction.MarkUnknownResult,
            classifySyncFailure(PendingOperationType.Create, 0, error),
        )
    }

    @Test
    fun `update conflict stays failed and is not treated as a duplicate create`() {
        val error = FinanceNetworkException.ClientFailure(409)

        assertEquals(
            SyncFailureAction.MarkFailed,
            classifySyncFailure(PendingOperationType.Update, 0, error),
        )
    }

    @Test
    fun `update network failure stays retryable`() {
        val error = FinanceNetworkException.Network(IOException("connection reset"))

        assertTrue(
            classifySyncFailure(PendingOperationType.Update, 0, error) is
                SyncFailureAction.RetryLater,
        )
    }

    @Test
    fun `pending transaction projections are reverted before account write`() {
        val result = calculateRemoteAccountBalance(
            displayedBalance = BigDecimal("125.00"),
            pendingImpacts = listOf(
                PendingBalanceImpact(
                    amount = BigDecimal("25.00"),
                    isIncome = true,
                ),
                PendingBalanceImpact(
                    amount = BigDecimal("10.00"),
                    isIncome = false,
                ),
            ),
        )

        assertEquals(BigDecimal("110.00"), result)
    }
}
