package com.zagirlek.finance.api.account

interface AccountsRepository {
    fun getAccounts(): List<Account>
}
