package dev.inmo.tools.telegram.webapps.client

import dev.inmo.micro_utils.common.Either
import dev.inmo.micro_utils.common.EitherFirst
import dev.inmo.micro_utils.common.EitherSecond
import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.micro_utils.coroutines.LinkedSupervisorJob
import dev.inmo.micro_utils.coroutines.launchSafelyWithoutExceptions
import io.ktor.client.content.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.w3c.xhr.FormData
import org.w3c.xhr.TEXT
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType

suspend fun uniPost(
    url: String,
    body: String,
    headers: Headers = Headers.Empty,
    onUpload: ProgressListener? = null
): Triple<String, Headers, HttpStatusCode> {
    val answer = CompletableDeferred<Triple<String, Headers, HttpStatusCode>>(currentCoroutineContext().job)
    val subscope = CoroutineScope(currentCoroutineContext().LinkedSupervisorJob())

    val request = XMLHttpRequest()
    headers.forEach { s, strings ->
        request.setRequestHeader(s, strings.joinToString())
    }
    request.responseType = XMLHttpRequestResponseType.TEXT
    onUpload ?.let {
        request.upload.onprogress = {
            subscope.launchSafelyWithoutExceptions { onUpload.onProgress(it.loaded.toLong(), it.total.toLong()) }
        }
    }
    request.onload = {
        val result = Triple(
            request.responseText,
            headers {
                request.getAllResponseHeaders().split("\n").forEach {
                    val key = it.takeWhile { it != ':' }
                    val value = it.removePrefix(key).removePrefix(":")
                    val headerValue = parseHeaderValue(value)
                    headerValue.forEach {
                        append(
                            key,
                            it.value
                        )
                    }
                }
            },
            HttpStatusCode.fromValue(request.status.toInt())
        )
        answer.complete(result)
    }
    request.onerror = {
        answer.completeExceptionally(Exception("Something went wrong: $it"))
    }
    request.open("POST", url, true)
    request.setRequestHeader("Content-Type", "application/json")
    request.send(body)

    answer.invokeOnCompletion {
        runCatching {
            if (request.readyState != XMLHttpRequest.DONE) {
                request.abort()
            }
        }
    }

    return answer.await().also {
        subscope.cancel()
    }
}
