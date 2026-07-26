package com.zagirlek.accounts

import com.zagirlek.ui.mvi.MviStore

interface AccountEditorComponent :
    MviStore<AccountEditorIntent, AccountEditorState, AccountEditorEffect>
