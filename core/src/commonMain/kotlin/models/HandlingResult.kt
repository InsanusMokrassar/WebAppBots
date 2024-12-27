package dev.inmo.tools.telegram.webapps.core.models

import dev.inmo.tools.telegram.webapps.core.utils.HttpStatusCodeSerializer
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
sealed interface HandlingResult<R : Any?> {
    val code: HttpStatusCode
    val data: R?
    @Serializable
    data class Success<R : Any?>(
        override val data: R,
        override val code: @Serializable(HttpStatusCodeSerializer::class) HttpStatusCode = HttpStatusCode.OK
    ) : HandlingResult<R>
    @Serializable
    data class Failure<R : Any?>(
        override val code: @Serializable(HttpStatusCodeSerializer::class) HttpStatusCode,
        override val data: R?
    ) : HandlingResult<R>

}
fun <R : Any?> HandlingResult<R>.copy(
    data: R,
    code: HttpStatusCode = this.code
) = when (this) {
    is HandlingResult.Failure -> HandlingResult.Failure<R>(code, data)
    is HandlingResult.Success -> HandlingResult.Success<R>(data, code)
}

fun <T : Any> T.requestHandlingSuccess(
    code: HttpStatusCode = HttpStatusCode.OK
) = HandlingResult.Success<T>(this, code)

fun <T : Any> HttpStatusCode.requestHandlingFailure(
    data: T? = null
) = HandlingResult.Failure<T>(this, data)

fun requestSuccessTrue(
    code: HttpStatusCode = HttpStatusCode.OK,
    data: Any? = null
) = HandlingResult.Success(data, code)

fun <T> HandlingResult<T>.dataOrError() = if (this is HandlingResult.Success) data else error("Unable to take data")
