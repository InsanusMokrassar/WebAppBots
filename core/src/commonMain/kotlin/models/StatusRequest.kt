package dev.inmo.tools.telegram.webapps.core.models

import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.tools.telegram.webapps.core.DefaultClient
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
object StatusRequest : BaseRequest<StatusRequest.Status> {
    @Serializable
    data class Status(
        val ok: Boolean,
        val freeMemoryInfo: String,
        val fileData: String? = null
    )

    override val resultSerializer: KSerializer<Status>
        get() = Status.serializer()
}

suspend fun DefaultClient.status() = request(StatusRequest)
suspend fun DefaultClient.status(file: MPPFile) = request(StatusRequest, file)
