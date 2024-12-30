import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.micro_utils.ktor.server.configurators.KtorApplicationConfigurator
import dev.inmo.micro_utils.ktor.server.createKtorServer
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.utils.TelegramAPIUrlsKeeper
import dev.inmo.tools.telegram.webapps.server.StatusRequestHandler
import dev.inmo.tools.telegram.webapps.server.includeTelegramWebAppsClientRequestHandling
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import java.io.File

fun main(args: Array<String>) {
    val telegramBotApiUrlsKeeper = TelegramAPIUrlsKeeper(
        token = args.first(),
        testServer = true
    )
    val port = args.getOrNull(1) ?.toIntOrNull() ?: 8080

    val server = createKtorServer(
        Netty,
        "0.0.0.0",
        port,
    ) {
        install(ContentNegotiation) {
            json(json)
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }

        routing {
            includeTelegramWebAppsClientRequestHandling(
                json,
                telegramBotApiUrlsKeeper,
                listOf(
                    StatusRequestHandler
                )
            )
            staticFiles(
                "/",
                staticDevelopmentFolder
            )
            staticFiles(
                "/",
                staticProductionFolder
            )
        }
    }

    server.start(wait = true)
}
