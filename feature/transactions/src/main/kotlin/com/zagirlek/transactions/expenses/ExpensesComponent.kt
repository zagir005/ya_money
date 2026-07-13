package com.zagirlek.transactions.expenses

import com.zagirlek.ui.mvi.MviStore

interface ExpensesComponent : MviStore<ExpensesIntent, ExpensesState, Nothing>
