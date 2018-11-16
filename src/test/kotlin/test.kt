
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
import us.kesslern.ascient.server
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
    fun testSimple() {
        withTestApplication(Application::server) {
            with(handleRequest(HttpMethod.Get, "/api/booleans")) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AscientTests::class.java)
    }
}