package dev.inmo.tools.telegram.webapps.client

import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.tools.telegram.webapps.core.models.BaseRequest
import dev.inmo.tools.telegram.webapps.core.models.HandlingResult
import io.ktor.utils.io.ByteReadChannel

interface DefaultClient {
    /**
     * Representation of [io.ktor.client.content.ProgressListener]. Has been created to avoid dependency on third-party lib
     */
    fun interface RequestProgressListener {
        fun onProgress(bytesSentTotal: Long, contentLength: Long?)
    }
    suspend fun <R : Any> request(payload: BaseRequest<R>, onUpload: RequestProgressListener? = null): HandlingResult<R>
    suspend fun <R : Any> request(payload: BaseRequest<R>, file: MPPFile, onUpload: RequestProgressListener? = null): HandlingResult<R>
}
