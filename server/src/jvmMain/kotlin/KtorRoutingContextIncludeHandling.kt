package dev.inmo.tools.telegram.webapps.server

import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.micro_utils.common.Warning
import dev.inmo.micro_utils.ktor.server.downloadToTemporalFile
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.TelegramAPIUrlsKeeper
import dev.inmo.tools.telegram.webapps.core.CommonWebAppConstants
import dev.inmo.tools.telegram.webapps.core.models.AuthorizedRequestBody
import dev.inmo.tools.telegram.webapps.core.models.HandlingResult
import dev.inmo.tools.telegram.webapps.core.models.RequestHandler
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

@OptIn(Warning::class)
fun Route.includeTelegramWebAppsClientRequestHandling(
    json: Json,
    telegramBotApiUrlsKeeper: TelegramAPIUrlsKeeper,
    requestsHandlers: List<RequestHandler>,
) {
    post(CommonWebAppConstants.requestAddress) {
        runCatching {
            val requestBody = call.receive<AuthorizedRequestBody>()

            handleRequest(json, telegramBotApiUrlsKeeper, requestBody) {
                requestsHandlers
                    .first { it.ableToHandle(requestBody.data) }
                    .handle(it.id.toChatId(), requestBody.data)
            }
        }.getOrElse {
            it.printStackTrace()
            throw it
        }
    }
    post(CommonWebAppConstants.multipartRequestAddress) {
        runCatching {
            val multipart = call.receiveMultipart()

            var data: AuthorizedRequestBody? = null
            var file: MPPFile? = null

            multipart.forEachPart {
                when (it.name) {
                    "data" -> {
                        data = when (it) {
                            is PartData.BinaryChannelItem,
                            is PartData.BinaryItem,
                            is PartData.FileItem -> {
                                return@forEachPart
                            }

                            is PartData.FormItem -> {
                                json.decodeFromString(AuthorizedRequestBody.serializer(), it.value)
                            }
                        }
                    }
                    "file" -> {
                        file = when (it) {
                            is PartData.BinaryChannelItem -> it.downloadToTemporalFile()
                            is PartData.BinaryItem -> it.downloadToTemporalFile()
                            is PartData.FileItem -> it.downloadToTemporalFile()
                            is PartData.FormItem -> { return@forEachPart }
                        }
                    }
                }
            }

            val capturedData = data
            val capturedFile = file
            if (capturedData == null || capturedFile == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    HandlingResult.Failure<Any>(HttpStatusCode.BadRequest, null) as HandlingResult<*>
                )
            } else {
                handleRequest(json, telegramBotApiUrlsKeeper, capturedData) {
                    requestsHandlers
                        .first { it.ableToHandle(capturedData.data, capturedFile) }
                        .handle(it.id.toChatId(), capturedData.data, capturedFile)
                }
            }
        }.getOrElse {
            it.printStackTrace()
            throw it
        }
    }
}
