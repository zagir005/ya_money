package com.zagirlek.finance.impl.account

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.CreateAccount
import com.zagirlek.finance.api.account.UpdateAccount

internal interface AccountsCommandHandler {
    suspend fun create(command: CreateAccount): AccountId

    suspend fun update(command: UpdateAccount)
}
