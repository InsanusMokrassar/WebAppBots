package dev.inmo.tools.telegram.webapps.client

import dev.inmo.kslog.common.e
import dev.inmo.kslog.common.logger
import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.micro_utils.common.either
import dev.inmo.tgbotapi.webapps.webApp
import dev.inmo.tools.telegram.webapps.core.CommonWebAppConstants
import dev.inmo.tools.telegram.webapps.core.models.AuthorizedRequestBody
import dev.inmo.tools.telegram.webapps.core.models.BaseRequest
import dev.inmo.tools.telegram.webapps.core.models.HandlingResult
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

/**
 * This realization assume that you have used some content serialization plugin for converting of incoming [BaseRequest]
 * to serialized format. Besides, it assumes that you have registered all polymorphic [BaseRequest] inheritors
 * in [json]
 */
class KtorDefaultClient(
    private val client: HttpClient,
    private val json: Json,
    private val initData: String = webApp.initData,
    private val initDataHash: String = webApp.initDataUnsafe.hash
) : DefaultClient {
    private suspend fun <R : Any> internalRequest(payload: BaseRequest<R>, file: MPPFile?): HandlingResult<R> {
        val result = runCatching {
            val serialized = json.encodeToString(
                AuthorizedRequestBody.serializer(),
                AuthorizedRequestBody(initData, initDataHash, payload)
            )
            val payloadFile = file
            val (body, headers, status) = if (payloadFile == null) {
                val request = client.post(CommonWebAppConstants.requestAddress) {
                    setBody(serialized)
                }
                Triple(request.bodyAsText(), request.headers, request.status)
            } else {
                client.uniUpload(
                    CommonWebAppConstants.multipartRequestAddress,
                    mapOf(
                        "data" to serialized.either<MPPFile, String>(),
                        "file" to payloadFile.either<MPPFile, String>()
                    ),
                )
            }
            val isSuccess = headers["internal_status_type"] == "success"
            val responseData = if (body.isNotBlank()) {
                json.decodeFromString(
                    payload.resultSerializer,
                    body
                )
            } else {
                null
            }
            when {
                isSuccess -> HandlingResult.Success<R>(responseData as R, status)
                else -> HandlingResult.Failure<R>(status, responseData as R)
            }
        }.getOrElse {
            logger.e(it)
            throw it
        }
        return result
    }

    override suspend fun <R : Any> request(payload: BaseRequest<R>): HandlingResult<R> {
        return internalRequest(payload, null)
    }

    override suspend fun <R : Any> request(payload: BaseRequest<R>, file: MPPFile): HandlingResult<R> {
        return internalRequest(payload, file)
    }
}
