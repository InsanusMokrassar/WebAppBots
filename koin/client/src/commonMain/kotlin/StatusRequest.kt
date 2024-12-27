package dev.inmo.tools.telegram.webapps.koin.client

import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.tools.telegram.webapps.client.DefaultClient
import dev.inmo.tools.telegram.webapps.core.models.StatusRequest

suspend fun DefaultClient.status() = request(StatusRequest)
suspend fun DefaultClient.status(file: MPPFile) = request(StatusRequest, file)
