package com.zagirlek.transactions

import com.zagirlek.ui.mvi.MviStore

interface TransactionEditorComponent : MviStore<
    TransactionEditorIntent,
    TransactionEditorState,
    TransactionEditorEffect
>
