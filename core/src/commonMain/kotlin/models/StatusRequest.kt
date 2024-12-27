package dev.inmo.tools.telegram.webapps.core.models

import dev.inmo.micro_utils.common.MPPFile
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
