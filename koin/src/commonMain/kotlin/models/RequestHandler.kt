package dev.inmo.tools.telegram.webapps.koin.models

import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tools.telegram.webapps.core.models.BaseRequest
import io.ktor.http.*
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import kotlin.reflect.KClass

interface RequestHandler {
    suspend fun ableToHandle(request: BaseRequest<*>): Boolean
    suspend fun ableToHandle(request: BaseRequest<*>, file: MPPFile): Boolean = ableToHandle(request)
    suspend fun handle(userId: UserId, request: BaseRequest<*>): HandlingResult<*>
    suspend fun handle(userId: UserId, request: BaseRequest<*>, file: MPPFile): HandlingResult<*> = HandlingResult.Failure(
        HttpStatusCode.MethodNotAllowed,
        null
    )

    abstract class ByType<T : BaseRequest<*>>(
        protected val requestKClass: KClass<T>
    ) : RequestHandler {
        override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = requestKClass.isInstance(request)
        abstract suspend fun handleTyped(userId: UserId, request: T): HandlingResult<*>
        open suspend fun handleTyped(userId: UserId, request: T, file: MPPFile): HandlingResult<*> = HandlingResult.Failure(
            HttpStatusCode.MethodNotAllowed,
            null
        )
        override suspend fun handle(userId: UserId, request: BaseRequest<*>): HandlingResult<*> {
            return if (requestKClass.isInstance(request)) {
                handleTyped(userId, request as T)
            } else {
                HandlingResult.Failure(HttpStatusCode.BadRequest, null)
            }
        }

        override suspend fun handle(userId: UserId, request: BaseRequest<*>, file: MPPFile): HandlingResult<*> {
            return if (requestKClass.isInstance(request)) {
                handleTyped(userId, request as T, file)
            } else {
                HandlingResult.Failure(HttpStatusCode.BadRequest, null)
            }
        }
    }
}

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
