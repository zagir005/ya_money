package com.zagirlek.finance.api.error

sealed interface NetworkError {
    val title: String
    val message: String

    data object Unauthorized : NetworkError {
        override val title: String = "Требуется авторизация"
        override val message: String = "Проверьте токен доступа и повторите попытку."
    }

    data class ClientFailure(
        val statusCode: Int,
    ) : NetworkError {
        override val title: String = "Не удалось выполнить запрос"
        override val message: String = "Сервер не смог обработать запрос. Повторите попытку позже."
    }

    data class ServerFailure(
        val statusCode: Int,
    ) : NetworkError {
        override val title: String = "Сервис временно недоступен"
        override val message: String = "Попробуйте обновить данные немного позже."
    }

    data class UnexpectedResponse(
        val statusCode: Int,
    ) : NetworkError {
        override val title: String = "Неожиданный ответ сервера"
        override val message: String = "Не удалось обработать ответ сервера. Повторите попытку."
    }

    data object Network : NetworkError {
        override val title: String = "Нет подключения к интернету"
        override val message: String = "Проверьте интернет-соединение и повторите попытку."
    }

    data object InvalidResponse : NetworkError {
        override val title: String = "Не удалось обработать данные"
        override val message: String = "Сервер вернул некорректные данные. Повторите попытку позже."
    }

    data object Unknown : NetworkError {
        override val title: String = "Не удалось загрузить данные"
        override val message: String = "Попробуйте обновить экран ещё раз."
    }
}

fun Throwable.toNetworkError(): NetworkError = when (this) {
    FinanceNetworkException.Unauthorized -> NetworkError.Unauthorized
    is FinanceNetworkException.ClientFailure -> NetworkError.ClientFailure(statusCode)
    is FinanceNetworkException.ServerFailure -> NetworkError.ServerFailure(statusCode)
    is FinanceNetworkException.UnexpectedResponse -> NetworkError.UnexpectedResponse(statusCode)
    is FinanceNetworkException.Network -> NetworkError.Network
    is FinanceNetworkException.InvalidResponse -> NetworkError.InvalidResponse
    else -> NetworkError.Unknown
}
