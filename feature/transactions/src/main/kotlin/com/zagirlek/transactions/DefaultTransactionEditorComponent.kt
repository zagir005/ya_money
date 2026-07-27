package com.zagirlek.transactions

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.category.CategoriesRepository
import com.zagirlek.finance.api.category.Category
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.api.transaction.CreateTransaction
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionsRepository
import com.zagirlek.finance.api.transaction.UpdateTransaction
import com.zagirlek.ui.cmp.MviComponent
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class DefaultTransactionEditorComponent(
    componentContext: ComponentContext,
    private val mode: TransactionEditorMode,
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val accountsRepository: AccountsRepository,
    private val onDismissRequested: () -> Unit,
    private val clock: Clock = Clock.systemDefaultZone(),
) : MviComponent<
    TransactionEditorState,
    TransactionEditorMutation,
    TransactionEditorIntent,
    TransactionEditorReducer
>(
    reducer = TransactionEditorReducer,
    componentContext = componentContext,
), TransactionEditorComponent {
    private val mutableState =
        MutableStateFlow<TransactionEditorState>(TransactionEditorState.Loading)

    override val state: StateFlow<TransactionEditorState> = mutableState.asStateFlow()
    override val effects: Flow<TransactionEditorEffect> = emptyFlow()

    init {
        observeEditorData()
        refreshReferenceData()
    }

    override fun accept(intent: TransactionEditorIntent) {
        when (intent) {
            is TransactionEditorIntent.AmountChanged ->
                TransactionEditorMutation.AmountChanged(intent.value).reduce(mutableState)
            is TransactionEditorIntent.CategorySelected ->
                TransactionEditorMutation.CategorySelected(intent.id).reduce(mutableState)
            is TransactionEditorIntent.AccountSelected ->
                TransactionEditorMutation.AccountSelected(intent.id).reduce(mutableState)
            is TransactionEditorIntent.DateSelected ->
                TransactionEditorMutation.DateSelected(intent.value).reduce(mutableState)
            is TransactionEditorIntent.TimeSelected ->
                TransactionEditorMutation.TimeSelected(intent.value).reduce(mutableState)
            is TransactionEditorIntent.SelectorOpened ->
                TransactionEditorMutation.SelectorChanged(intent.selector).reduce(mutableState)
            TransactionEditorIntent.SelectorDismissed ->
                TransactionEditorMutation.SelectorChanged(null).reduce(mutableState)
            TransactionEditorIntent.SaveClicked -> save()
            TransactionEditorIntent.Dismissed -> onDismissRequested()
        }
    }

    private fun observeEditorData() {
        val transactionFlow = when (val value = mode) {
            is TransactionEditorMode.Create -> flowOf(null)
            is TransactionEditorMode.Edit ->
                transactionsRepository.observeTransaction(value.transactionId)
        }

        componentScope.launch {
            combine(
                categoriesRepository.observeCategories(),
                accountsRepository.observeAccounts(),
                transactionFlow,
            ) { categories, accounts, transaction ->
                EditorData(categories, accounts, transaction)
            }.collect { data ->
                val current = mutableState.value as? TransactionEditorState.Content
                val content = current?.mergeOptions(data) ?: data.toInitialContent()
                if (content != null) {
                    TransactionEditorMutation.Loaded(content).reduce(mutableState)
                }
            }
        }
    }

    private fun refreshReferenceData() {
        ioScope.launch {
            try {
                categoriesRepository.refreshCategories()
                accountsRepository.refreshAccounts()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Room remains the source of truth. A failed background refresh
                // must not replace an offline-capable editor with a network error.
            }
        }
    }

    private fun save() {
        val state = mutableState.value as? TransactionEditorState.Content ?: return
        if (state.isSaving) return

        val amount = state.amountInput
            .replace(',', '.')
            .toBigDecimalOrNull()
            ?.takeIf { it.signum() > 0 }
        val account = state.accounts.firstOrNull { it.id == state.selectedAccountId }
        val categoryId = state.selectedCategoryId
        if (amount == null || account == null || categoryId == null) {
            return
        }

        TransactionEditorMutation.Saving.reduce(mutableState)
        ioScope.launch {
            try {
                val occurredAt = state.date
                    .atTime(state.time)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                val money = Money(
                    amount = amount,
                    currency = com.zagirlek.finance.api.money.CurrencyCode.parse(account.currency),
                )
                when (val value = mode) {
                    is TransactionEditorMode.Create -> transactionsRepository.createTransaction(
                        CreateTransaction(
                            accountId = account.id,
                            categoryId = categoryId,
                            money = money,
                            occurredAt = occurredAt,
                            comment = null,
                        ),
                    )
                    is TransactionEditorMode.Edit -> {
                        val original = transactionsRepository.observeTransaction(value.transactionId)
                            .firstExisting()
                        transactionsRepository.updateTransaction(
                            UpdateTransaction(
                                transactionId = value.transactionId,
                                accountId = account.id,
                                categoryId = categoryId,
                                money = money,
                                occurredAt = occurredAt,
                                comment = original.comment,
                            ),
                        )
                    }
                }
                componentScope.launch {
                    onDismissRequested()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                componentScope.launch {
                    TransactionEditorMutation.SaveFailed(
                        error.message ?: "Не удалось сохранить операцию.",
                    ).reduce(mutableState)
                }
            }
        }
    }

    private fun EditorData.toInitialContent(): TransactionEditorState.Content? {
        val transaction = transaction
        val type = when (val value = mode) {
            is TransactionEditorMode.Create -> value.type
            is TransactionEditorMode.Edit -> transaction?.category?.type ?: return null
        }
        val zoneId = ZoneId.systemDefault()
        val dateTime = (transaction?.occurredAt ?: Instant.now(clock)).atZone(zoneId)
        val filteredCategories = categories.filter { it.type == type }

        return TransactionEditorState.Content(
            type = type,
            amountInput = transaction?.money?.amount?.toPlainString().orEmpty(),
            categories = filteredCategories.map(Category::toUi),
            selectedCategoryId = transaction?.category?.id ?: filteredCategories.firstOrNull()?.id,
            accounts = accounts.map(Account::toUi),
            selectedAccountId = transaction?.accountId ?: accounts.firstOrNull()?.id,
            date = dateTime.toLocalDate(),
            time = dateTime.toLocalTime().withSecond(0).withNano(0),
        )
    }

    private fun TransactionEditorState.Content.mergeOptions(
        data: EditorData,
    ): TransactionEditorState.Content {
        val categories = data.categories.filter { it.type == type }.map(Category::toUi)
        val accounts = data.accounts.map(Account::toUi)
        return copy(
            categories = categories,
            selectedCategoryId = selectedCategoryId.takeIf { selected ->
                categories.any { it.id == selected }
            } ?: categories.firstOrNull()?.id,
            accounts = accounts,
            selectedAccountId = selectedAccountId.takeIf { selected ->
                accounts.any { it.id == selected }
            } ?: accounts.firstOrNull()?.id,
        )
    }
}

private data class EditorData(
    val categories: List<Category>,
    val accounts: List<Account>,
    val transaction: Transaction?,
)

private fun Category.toUi() = TransactionEditorCategoryUi(
    id = id,
    name = name,
    emoji = emoji,
)

private fun Account.toUi() = TransactionEditorAccountUi(
    id = id,
    name = name,
    emoji = emoji,
    currency = money.currency.value,
)

private suspend fun Flow<Transaction?>.firstExisting(): Transaction =
    first { it != null }
        ?: error("Transaction was not found.")
