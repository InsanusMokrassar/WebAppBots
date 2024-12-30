import dev.inmo.tools.telegram.webapps.core.models.BaseRequest
import dev.inmo.tools.telegram.webapps.core.models.StatusRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val json = Json {
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        polymorphic(BaseRequest::class, StatusRequest::class, StatusRequest.serializer())
    }
}
