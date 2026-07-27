package com.zagirlek.finance.impl.sync

import com.zagirlek.finance.api.error.FinanceNetworkException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ServerRetryPolicyTest {
    @Test
    fun `server request succeeds on third attempt after two fixed delays`() = runBlocking {
        var attempts = 0
        val delays = mutableListOf<Long>()

        val result = retryServerFailures(
            retryDelay = delays::add,
        ) {
            attempts += 1
            if (attempts < 3) {
                throw FinanceNetworkException.ServerFailure(500)
            }
            "success"
        }

        assertEquals("success", result)
        assertEquals(3, attempts)
        assertEquals(listOf(2_000L, 2_000L), delays)
    }

    @Test
    fun `third server failure is returned to caller`() {
        var attempts = 0
        val delays = mutableListOf<Long>()

        assertThrows(FinanceNetworkException.ServerFailure::class.java) {
            runBlocking {
                retryServerFailures(
                    retryDelay = delays::add,
                ) {
                    attempts += 1
                    throw FinanceNetworkException.ServerFailure(500)
                }
            }
        }

        assertEquals(3, attempts)
        assertEquals(listOf(2_000L, 2_000L), delays)
    }
}
