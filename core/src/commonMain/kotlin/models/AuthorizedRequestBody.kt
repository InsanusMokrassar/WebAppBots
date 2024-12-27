package dev.inmo.tools.telegram.webapps.core.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class AuthorizedRequestBody(
    val initData: String,
    val initDataHash: String,
    val data: BaseRequest<out @Contextual Any?>
)