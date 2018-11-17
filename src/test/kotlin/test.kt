
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference
import us.kesslern.ascient.AscientBoolean
import us.kesslern.ascient.Booleans
import us.kesslern.ascient.server
import java.util.*
import kotlin.test.assertEquals


class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>()

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AscientTests {

    init {
        logger.info("Starting...")
        KPostgreSQLContainer().start()
        Flyway
                .configure()
                .dataSource("jdbc:tc:postgresql:9.6.8://hostname/databasename?TC_DAEMON=true", "", "")
                .load()
                .migrate()
        Database.connect("jdbc:tc:postgresql:9.6.8://hostname/databasename?TC_DAEMON=true", "org.postgresql.Driver", "", "")
    }

    @Test
    fun `test boolean CRUD operations`() {
        withTestApplication(Application::server) {
            // insert new boolean with random UUID as name and record the new ID
            val name = UUID.randomUUID()
            val newId: Int = with(handleRequest(
                HttpMethod.Post,
                "/api/booleans?name=${name}&value=${true}")) {
                assertEquals(HttpStatusCode.OK, response.status())
                response.content?.toInt() ?: throw RuntimeException("Response was not convertible to Int")
            }

            // verify boolean inserted with value and ID
            with(handleRequest(HttpMethod.Get, "/api/booleans/${newId}")) {
                val newBoolean = mapper.readValue(response.content, AscientBoolean::class.java)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(true, newBoolean.value)
            }

            // change value to false
            with(handleRequest(HttpMethod.Put, "/api/booleans/${newId}?value=${false}")) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            // get all values and verify
            with(handleRequest(HttpMethod.Get, "/api/booleans")) {
                val newBooleans: List<AscientBoolean> = mapper.readValue(response.content ?: throw RuntimeException())
                assertEquals(HttpStatusCode.OK, response.status())
                val newValue = newBooleans.find { it.id == newId }?.value
                assertEquals(false, newValue)
            }

            // delete
            with(handleRequest(HttpMethod.Delete, "/api/booleans/${newId}")) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            // get all values and verify
            with(handleRequest(HttpMethod.Get, "/api/booleans")) {
                val newBooleans: List<AscientBoolean> = ObjectMapper().readValue(response.content ?: throw RuntimeException())
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(null, newBooleans.find { it.id == newId })
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AscientTests::class.java)
        private val mapper = ObjectMapper().registerModule(KotlinModule())
    }
}