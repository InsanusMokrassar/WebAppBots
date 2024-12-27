package dev.inmo.tools.telegram.webapps.server

import dev.inmo.micro_utils.common.Warning
import dev.inmo.tgbotapi.utils.TelegramAPIUrlsKeeper
import dev.inmo.tools.telegram.webapps.core.models.AuthorizedRequestBody
import dev.inmo.tools.telegram.webapps.core.models.HandlingResult
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

@Warning("This feature can be changed without any notices")
suspend fun RoutingContext.handleRequest(
    json: Json,
    telegramBotApiUrlsKeeper: TelegramAPIUrlsKeeper,
    requestBody: AuthorizedRequestBody,
    handlingBlock: suspend (InitDataInfo.UserInfo) -> HandlingResult<*>
) {
    runCatching {
        val authorized = telegramBotApiUrlsKeeper.checkWebAppData(
            requestBody.initData,
            requestBody.initDataHash
        )

        if (authorized) {
            val userData = requestBody.initData.decodeUrlQueryToMap()["user"] ?.firstOrNull()
            if (userData == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@handleRequest
            }
            val info = json.decodeFromString(InitDataInfo.UserInfo.serializer(), userData)
            val handlingResult = handlingBlock(info)

            val serializedData = handlingResult.data ?.let {
                json.encodeToString(
                    requestBody.data.resultSerializer as KSerializer<Any?>,
                    handlingResult.data
                )
            }
            val isSuccess = handlingResult is HandlingResult.Success
            call.response.header("internal_status_type", if (isSuccess) "success" else "failure")

            serializedData ?.let {
                call.respond(handlingResult.code, it)
            } ?: call.respond(handlingResult.code)

        } else {
            call.respond(
                HttpStatusCode.Unauthorized,
                HandlingResult.Failure<Any>(HttpStatusCode.Unauthorized, null) as HandlingResult<*>
            )
        }
    }.getOrElse {
        it.printStackTrace()
        throw it
    }
}
