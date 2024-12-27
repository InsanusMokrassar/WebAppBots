package dev.inmo.tools.telegram.webapps.client

import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.tools.telegram.webapps.core.models.BaseRequest
import dev.inmo.tools.telegram.webapps.core.models.HandlingResult

interface DefaultClient {
    suspend fun <R : Any> request(payload: BaseRequest<R>): HandlingResult<R>
    suspend fun <R : Any> request(payload: BaseRequest<R>, file: MPPFile): HandlingResult<R>
}
