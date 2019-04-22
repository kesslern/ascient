package us.kesslern.ascient.util

import io.ktor.application.Application
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.call
import io.ktor.client.request.header
import io.ktor.client.response.readText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import us.kesslern.ascient.TestContext
import us.kesslern.ascient.server
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

data class Header(
        val name: String,
        val value: String
)

data class UnifiedResponse(
        val status: HttpStatusCode?,
        val content: String?
)

@ExperimentalContracts
fun request(
        method: HttpMethod,
        uri: String,
        body: Any? = null,
        authenticated: Boolean = true,
        sessionId: String? = null,
        handler: (UnifiedResponse.() -> Unit)
): UnifiedResponse {
    contract {
        callsInPlace(handler, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }

    val response = request(method, uri, body, authenticated, sessionId)
    response.let(handler)
    return response
}

private fun request(
        method: HttpMethod,
        uri: String,
        body: Any? = null,
        authenticated: Boolean = true,
        sessionId: String? = null
): UnifiedResponse {
    val headers: MutableList<Header> = mutableListOf()
    if (authenticated) {
        headers.add(Header("X-AscientUsername", "admin"))
        headers.add(Header("X-AscientPassword", "password"))
    }
    if (sessionId !== null) headers.add(Header("X-AscientSession", sessionId))
    val json = body?.toJson()

    return if (TestContext.useRealBackend) {
        runBlocking {
            with(requestWithBackend(method, TestContext.backend + uri, json, headers)) {
                UnifiedResponse(response.status, response.readText())
            }
        }
    } else {
        with(requestWithMockKtor(method, uri, json, headers)) {
            UnifiedResponse(response.status(), response.content)
        }
    }
}

private fun requestWithMockKtor(
        method: HttpMethod,
        uri: String,
        body: String?,
        headers: List<Header>
): TestApplicationCall =
        withTestApplication(Application::server) {
            handleRequest(method, uri) {
                headers.forEach { addHeader(it.name, it.value) }
                if (body != null) {
                    addHeader(HttpHeaders.ContentType, "application/json")
                    setBody(body)
                }
            }
        }

private suspend fun requestWithBackend(
        method: HttpMethod,
        uri: String,
        body: String?,
        headers: List<Header>
): HttpClientCall = TestContext.client.call(uri) {
    this.method = method
    if (body != null) {
        this.body = TextContent(body, contentType = ContentType.Application.Json)
    }
    headers.forEach { this.header(it.name, it.value) }
}
