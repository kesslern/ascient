
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.PostgreSQLContainer


class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>()

object TestContext {
    val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    const val databaseDriver = "org.postgresql.Driver"
    const val databaseConnection = "jdbc:tc:postgresql:9.6.8://hostname/databasename?TC_DAEMON=true"

    val client = HttpClient(Apache)
    var backend: String = System.getProperty("ascient.backend")
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


fun request(method: HttpMethod, uri: String): UnifiedResponse {
    return if (TestContext.useRealBackend) {
        runBlocking {
            with(requestWithBackend(method, TestContext.backend + uri)) {
                UnifiedResponse(response.status, response.readText())
            }
        }
    } else {
        with(requestWithMockKtor(method, uri)) {
            UnifiedResponse(response.status(), response.content)
        }
    }
}

fun requestWithMockKtor(
        method: HttpMethod,
        uri: String
): TestApplicationCall =
        withTestApplication(Application::server) {
            handleRequest(method, uri)
        }

suspend fun requestWithBackend(
        method: HttpMethod,
        uri: String
): HttpClientCall = TestContext.client.call(uri) {
    this.method = method
}

data class UnifiedResponse(
        val status: HttpStatusCode?,
        val content: String?
)