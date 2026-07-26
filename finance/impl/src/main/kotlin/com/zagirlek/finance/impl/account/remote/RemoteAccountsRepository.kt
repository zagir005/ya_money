package com.zagirlek.finance.impl.account.remote

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.account.CreateAccount
import com.zagirlek.finance.api.account.UpdateAccount
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

    override suspend fun createAccount(command: CreateAccount): AccountId = withContext(ioContext) {
        httpClient.post<AccountCreateRequestDto, AccountDto>(
            path = ACCOUNTS_PATH,
            body = AccountCreateRequestDto(
                name = command.name.trim(),
                emoji = command.emoji.trim(),
                balance = command.initialBalance.amount.toPlainString(),
                currency = command.initialBalance.currency.value,
            ),
        ).toDomain().id
    }

    override suspend fun updateAccount(command: UpdateAccount) {
        val account = requireAccount(command.accountId)
        updateRemote(
            account = account,
            name = command.name.trim(),
            emoji = command.emoji.trim(),
            balance = command.balance.amount.toPlainString(),
            currency = command.balance.currency.value,
        )
    }

    override suspend fun getAccounts(): List<Account> = loadAccounts()

    private suspend fun loadAccounts(): List<Account> = withContext(ioContext) {
        httpClient.get<List<AccountDto>>(path = ACCOUNTS_PATH).map(AccountDto::toDomain)
    }

    private suspend fun requireAccount(id: AccountId): Account =
        requireNotNull(loadAccounts().firstOrNull { it.id == id }) {
            "Account ${id.value} was not found."
        }

    private suspend fun updateRemote(
        account: Account,
        name: String,
        emoji: String,
        balance: String,
        currency: String,
    ) = withContext(ioContext) {
        val remoteId = account.id.value.toLong()
        httpClient.put<AccountUpdateRequestDto, AccountDto>(
            path = "$ACCOUNTS_PATH/$remoteId",
            body = AccountUpdateRequestDto(
                name = name,
                emoji = emoji,
                balance = balance,
                currency = currency,
            ),
        )
    }

    private companion object {
        const val ACCOUNTS_PATH = "accounts"
    }
}
