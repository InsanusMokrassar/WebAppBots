package dev.inmo.tools.telegram.webapps.koin.models

import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tools.telegram.webapps.core.models.BaseRequest
import dev.inmo.tools.telegram.webapps.core.models.HandlingResult
import dev.inmo.tools.telegram.webapps.core.models.RequestHandler
import io.ktor.http.*
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import kotlin.reflect.KClass

fun Module.registerRequestHandler(handler: RequestHandler) {
    singleWithRandomQualifier<RequestHandler> { handler }
}

fun Module.registerRequestHandler(
    createdAtStart: Boolean = false,
    definition: Definition<RequestHandler>
) {
    singleWithRandomQualifier<RequestHandler>(createdAtStart, definition)
}

fun Module.registerRequestHandler(ableToHandle: (BaseRequest<*>) -> Boolean, handle: (BaseRequest<*>) -> HandlingResult<*>) {
    registerRequestHandler(
        object : RequestHandler {
            override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = ableToHandle(request)
            override suspend fun handle(userId: UserId, request: BaseRequest<*>): HandlingResult<*> = handle(request)
        }
    )
}
