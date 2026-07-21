package com.zagirlek.finance.api.error

sealed class FinanceNetworkException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    data object Unauthorized : FinanceNetworkException(
        message = "Не удалось авторизоваться. Проверьте токен доступа.",
    )

    data class ClientFailure(
        val statusCode: Int,
    ) : FinanceNetworkException(
        message = "Сервер не смог обработать запрос (код $statusCode).",
    )

    data class ServerFailure(
        val statusCode: Int,
    ) : FinanceNetworkException(
        message = "Сервер временно недоступен (код $statusCode).",
    )

    data class UnexpectedResponse(
        val statusCode: Int,
    ) : FinanceNetworkException(
        message = "Сервер вернул неожиданный ответ (код $statusCode).",
    )

    data class Network(
        override val cause: Throwable,
    ) : FinanceNetworkException(
        message = "Не удалось подключиться к серверу. Проверьте интернет-соединение.",
        cause = cause,
    )

    data class InvalidResponse(
        override val cause: Throwable,
    ) : FinanceNetworkException(
        message = "Сервер вернул некорректные данные.",
        cause = cause,
    )
}
