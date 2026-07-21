package com.zagirlek.finance.impl.network

import com.zagirlek.finance.api.error.FinanceNetworkException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.ContentConvertException
import java.io.Closeable
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class FinanceHttpClient(
    token: String,
    private val baseUrl: String = DEFAULT_BASE_URL,
    @PublishedApi
    internal val client: HttpClient = createClient(token),
) : Closeable {

    init {
        require(token.isNotBlank()) { "Finance API token must not be blank." }
    }

    suspend inline fun <reified Response : Any> get(
        path: String,
        noinline configure: HttpRequestBuilder.() -> Unit = {},
    ): Response = request {
        client.get(requestUrl(path)) {
            configure()
        }
    }

    suspend inline fun <reified Response : Any> request(
        crossinline request: suspend () -> HttpResponse,
    ): Response = try {
        val response = request()
        response.throwIfUnsuccessful()
        response.body()
    } catch (error: CancellationException) {
        throw error
    } catch (error: FinanceNetworkException) {
        throw error
    } catch (error: SerializationException) {
        throw FinanceNetworkException.InvalidResponse(error)
    } catch (error: ContentConvertException) {
        throw FinanceNetworkException.InvalidResponse(error)
    } catch (error: IOException) {
        throw FinanceNetworkException.Network(error)
    }

    override fun close() {
        client.close()
    }

    @PublishedApi
    internal fun requestUrl(path: String): String = "${baseUrl.trimEnd('/')}/${path.trimStart('/')}"

    @PublishedApi
    internal fun HttpResponse.throwIfUnsuccessful() {
        if (status.isSuccess()) return

        throw status.toFinanceDataException()
    }

    private fun HttpStatusCode.toFinanceDataException(): FinanceNetworkException = when (value) {
        HttpStatusCode.Unauthorized.value -> FinanceNetworkException.Unauthorized
        in 400..499 -> FinanceNetworkException.ClientFailure(value)
        in 500..599 -> FinanceNetworkException.ServerFailure(value)
        else -> FinanceNetworkException.UnexpectedResponse(value)
    }

    private companion object {
        const val DEFAULT_BASE_URL = "https://shmr-finance.ru/api/v1"

        fun createClient(token: String): HttpClient = HttpClient(OkHttp) {
            expectSuccess = false

            defaultRequest {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                )
            }
        }
    }
}
