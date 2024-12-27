package dev.inmo.tools.telegram.webapps.koin.models

import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.tools.telegram.webapps.core.models.BaseRequest
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.koin.core.module.Module

@OptIn(InternalSerializationApi::class)
inline fun <reified T : BaseRequest<*>> Module.registerRequestType() {
    singleWithRandomQualifier {
        SerializersModule {
            polymorphic(Any::class, T::class, T::class.serializer())
            polymorphic(BaseRequest::class, T::class, T::class.serializer())
        }
    }
}
