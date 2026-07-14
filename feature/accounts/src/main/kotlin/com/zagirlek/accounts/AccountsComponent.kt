package com.zagirlek.accounts

import com.zagirlek.ui.mvi.MviStore

interface AccountsComponent : MviStore<AccountsIntent, AccountsState, AccountsEffect>
