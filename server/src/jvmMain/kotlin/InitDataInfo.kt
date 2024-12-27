package dev.inmo.tools.telegram.webapps.server

import dev.inmo.tgbotapi.types.RawChatId
import kotlinx.serialization.Serializable

@Serializable
data class InitDataInfo(
    val userInfo: UserInfo
) {
    @Serializable
    data class UserInfo(
        val id: RawChatId
    )
}
