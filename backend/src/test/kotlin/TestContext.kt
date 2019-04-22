package us.kesslern.ascient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
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
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>()

data class Header(val name: String, val value: String)

data class UnifiedResponse(
        val status: HttpStatusCode?,
        val content: String?
)

object TestContext {
    val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JodaModule())

    private const val databaseDriver = "org.postgresql.Driver"
    private const val databaseConnection = "jdbc:tc:postgresql:9.6.8://hostname/databasename?TC_DAEMON=true"

    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
    var backend: String = System.getProperty("ascient.backend", "")
    var useRealBackend = !backend.isEmpty()

    init {
        if (!useRealBackend) {
            KPostgreSQLContainer().start()
            Flyway
                    .configure()
                    .dataSource(databaseConnection, "", "")
                    .load()
                    .migrate()
            Database.connect(databaseConnection, databaseDriver)
        }
    }
}

inline fun <reified T> readJson(content: String?): T {
    if (content == null) throw RuntimeException("Empty Content")
    return TestContext.mapper.readValue(content)
}

inline fun Any.toJson(): String = TestContext.mapper.writeValueAsString(this)

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

fun request(
        method: HttpMethod,
        uri: String,
        body: Any? = null,
        authenticated: Boolean = true,
        sessionId: String? = null
): UnifiedResponse {
    val headers = ArrayList<Header>()
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
