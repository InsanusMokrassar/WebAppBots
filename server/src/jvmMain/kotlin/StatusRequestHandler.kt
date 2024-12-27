package dev.inmo.tools.telegram.webapps.server

import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.micro_utils.common.fixed
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tools.telegram.webapps.core.models.*

object StatusRequestHandler : RequestHandler {
    override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = request == StatusRequest
    override suspend fun ableToHandle(request: BaseRequest<*>, file: MPPFile): Boolean = request == StatusRequest

    override suspend fun handle(userId: UserId, request: BaseRequest<*>): HandlingResult<StatusRequest.Status> {
        val casted = (request as StatusRequest)
        val runtime = Runtime.getRuntime()
        return StatusRequest.Status(
            true,
            "${runtime.freeMemory() / 1024 / 1024} MB / ${runtime.totalMemory() / 1024 / 1024} MB || ${
                (runtime.freeMemory().toDouble() / runtime.totalMemory().toDouble() * 100).fixed(2)
            }%",
            null
        ).requestHandlingSuccess()
    }

    override suspend fun handle(userId: UserId, request: BaseRequest<*>, file: MPPFile): HandlingResult<*> {
        val casted = (request as StatusRequest)
        val runtime = Runtime.getRuntime()
        return StatusRequest.Status(
            true,
            "${runtime.freeMemory() / 1024 / 1024} MB / ${runtime.totalMemory() / 1024 / 1024} MB || ${
                (runtime.freeMemory().toDouble() / runtime.totalMemory().toDouble() * 100).fixed(2)
            }%",
            file.readText()
        ).requestHandlingSuccess()
    }
}
