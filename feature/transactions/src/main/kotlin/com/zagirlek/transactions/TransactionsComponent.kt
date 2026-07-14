package com.zagirlek.transactions

import com.zagirlek.ui.mvi.MviStore

interface TransactionsComponent : MviStore<TransactionsIntent, TransactionsState, TransactionsEffect>
