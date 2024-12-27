package dev.inmo.tools.telegram.webapps.core.models

import kotlinx.serialization.KSerializer

/**
 * To realize request:
 *
 * 1. Create [BaseRequest] realization
 * 2. Create result type which will be used as an [R] argument
 * 3. Register your request in plugin with [registerRequestType] in `setupDI`
 * 4. Add realization of [RequestHandler]
 * 5. Register realization of RequestHandler using [registerRequestHandler] in `setupDI` in commonMain of webapp.server
 *
 * @see StatusRequest
 * @see dev.inmo.tools.telegram.webapps.core.StatusRequestHandler
 */
interface BaseRequest<R> {
    val resultSerializer: KSerializer<R>
}
