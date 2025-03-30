package dev.inmo.tools.telegram.webapps.client

import dev.inmo.kslog.common.e
import dev.inmo.kslog.common.logger
import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.micro_utils.common.either
import dev.inmo.tgbotapi.webapps.webApp
import dev.inmo.tools.telegram.webapps.client.DefaultClient.RequestProgressListener
import dev.inmo.tools.telegram.webapps.core.CommonWebAppConstants
import dev.inmo.tools.telegram.webapps.core.models.AuthorizedRequestBody
import dev.inmo.tools.telegram.webapps.core.models.BaseRequest
import dev.inmo.tools.telegram.webapps.core.models.HandlingResult
import io.ktor.client.content.ProgressListener
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * This realization assume that you have used some content serialization plugin for converting of incoming [BaseRequest]
 * to serialized format. Besides, it assumes that you have registered all polymorphic [BaseRequest] inheritors
 * in [json]
 */
class FetchDefaultClient(
    private val dataSerializer: (BaseRequest<*>) -> String,
    private val dataDeserializer: (String, KSerializer<*>) -> Any?,
) : DefaultClient {
    constructor(
        json: Json,
        initData: String = webApp.initData,
        initDataHash: String = webApp.initDataUnsafe.hash
    ) : this(
        {
            json.encodeToString(
                AuthorizedRequestBody.serializer(),
                AuthorizedRequestBody(initData, initDataHash, it)
            )
        },
        { body, serializer ->
            json.decodeFromString(
                serializer,
                body
            )
        }
    )

    private suspend fun <R : Any> internalRequest(payload: BaseRequest<R>, file: MPPFile?, onUpload: RequestProgressListener? = null): HandlingResult<R> {
        val result = runCatching {
            val serialized = dataSerializer(payload)
            val payloadFile = file
            val (body, headers, status) = if (payloadFile == null) {
                uniPost(
                    CommonWebAppConstants.requestAddress,
                    serialized,
                    onUpload = onUpload  ?.let {
                        ProgressListener { bytesSentTotal, contentLength -> it.onProgress(bytesSentTotal, contentLength)}
                    },
                )
            } else {
                uniUpload(
                    CommonWebAppConstants.multipartRequestAddress,
                    mapOf(
                        "data" to serialized.either<MPPFile, String>(),
                        "file" to payloadFile.either<MPPFile, String>()
                    ),
                    onUpload = onUpload  ?.let {
                        ProgressListener { bytesSentTotal, contentLength -> it.onProgress(bytesSentTotal, contentLength)}
                    },
                )
            }
            val isSuccess = headers["internal_status_type"] == "success"
            val responseData = if (body.isNotBlank()) {
                dataDeserializer(
                    body,
                    payload.resultSerializer
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
            HandlingResult.Failure<R>(HttpStatusCode.SeeOther, null)
        }
        return result
    }

    override suspend fun <R : Any> request(payload: BaseRequest<R>, onUpload: RequestProgressListener?): HandlingResult<R> {
        return internalRequest(payload, null, onUpload)
    }

    override suspend fun <R : Any> request(payload: BaseRequest<R>, file: MPPFile, onUpload: RequestProgressListener?): HandlingResult<R> {
        return internalRequest(payload, file, onUpload)
    }
}
