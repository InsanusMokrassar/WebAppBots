import dev.inmo.tools.telegram.webapps.client.FetchDefaultClient
import dev.inmo.tools.telegram.webapps.client.status
import dev.inmo.tools.telegram.webapps.core.models.HandlingResult
import dev.inmo.tools.telegram.webapps.core.models.StatusRequest
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main() {
    val scope = CoroutineScope(Dispatchers.Default)
    scope.launch {
        val client = FetchDefaultClient(json)
        val response = client.status()

        document.body ?.append(
            document.createElement("div").apply {
                innerHTML = when (response) {
                    is HandlingResult.Failure<*> -> {
                        response.code.toString()
                    }
                    is HandlingResult.Success<StatusRequest.Status> -> {
                        """
                        Ok: ${response.data.ok}; ${response.data.freeMemoryInfo}
                    """.trimIndent()
                    }
                }
            }
        )
    }
}