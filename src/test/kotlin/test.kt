import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>()

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AscientTests {

    private val logger = LoggerFactory.getLogger(AscientTests::class.java)
    private val mapper = ObjectMapper().registerModule(KotlinModule())

    private val databaseDriver = "org.postgresql.Driver"
    private val databaseConnection = "jdbc:tc:postgresql:9.6.8://hostname/databasename?TC_DAEMON=true"

    init {
        logger.info("Starting...")
        KPostgreSQLContainer().start()
        Flyway
                .configure()
                .dataSource(databaseConnection, "", "")
                .load()
                .migrate()
        Database.connect(databaseConnection, databaseDriver)
    }

    @Test
    fun `test boolean CRUD operations`() {
        // insert new boolean with random UUID as name and record the new ID
        val name = UUID.randomUUID()
        val newId = with(request(
            HttpMethod.Post,
            "/api/booleans?name=$name&value=${true}")) {
            assertEquals(HttpStatusCode.OK, response.status())
            val content = response.content
            assertNotNull(content)
            content.toInt()
        }

        // verify boolean inserted with value and ID
        with(request(HttpMethod.Get, "/api/booleans/$newId")) {
            val newBoolean = mapper.readValue(response.content, BooleanDBO::class.java)
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(true, newBoolean.value)
        }

        // change value to false
        with(request(HttpMethod.Put, "/api/booleans/$newId?value=${false}")) {
            assertEquals(HttpStatusCode.NoContent, response.status())
        }

        // get all values and verify
        with(request(HttpMethod.Get, "/api/booleans")) {
            val content = response.content
            assertNotNull(content)
            val newBooleans: List<BooleanDBO> = mapper.readValue(content)
            assertEquals(HttpStatusCode.OK, response.status())
            val newValue = newBooleans.find { it.id == newId }?.value
            assertEquals(false, newValue)
        }

        // delete
        with(request(HttpMethod.Delete, "/api/booleans/$newId")) {
            assertEquals(HttpStatusCode.NoContent, response.status())
        }

        // get all values and verify
        with(request(HttpMethod.Get, "/api/booleans")) {
            val newBooleans: List<BooleanDBO> = mapper.readValue(response.content ?: throw RuntimeException())
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(null, newBooleans.find { it.id == newId })
        }
    }

    @Test
    fun `test default parameters`() {
        // POST without a value
        val id = with(request(HttpMethod.Post, "/api/booleans?name=${UUID.randomUUID()}")) {
            assertEquals(HttpStatusCode.OK, response.status())
            response.content?.toInt()
        }

        // Verify inserted with value True
        with(request(HttpMethod.Get, "/api/booleans/$id")) {
            val newBoolean = mapper.readValue(response.content, BooleanDBO::class.java)
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(true, newBoolean.value)
        }
    }

    @Test
    fun `test bad parameters`() {
        with(request(HttpMethod.Get, "/api/booleans/9999999")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }

        with(request(HttpMethod.Get, "/api/booleans/-1")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
         }

            with(request(HttpMethod.Post, "/api/booleans")) {
            assertEquals("Missing parameter: name", response.content)
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }

        val id = with(request(HttpMethod.Post, "/api/booleans?name=${UUID.randomUUID()}")) {
            assertEquals(HttpStatusCode.OK, response.status())
            response.content?.toInt()
        }

            with(request(HttpMethod.Put, "/api/booleans/$id")) {
            assertEquals("Missing parameter: value", response.content)
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }
}

fun request(
    method: HttpMethod,
    uri: String
): TestApplicationCall =
    withTestApplication(Application::server) {
        handleRequest(method, uri)
    }