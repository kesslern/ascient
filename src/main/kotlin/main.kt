
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database


fun main() {
    val databaseConnection: String = System.getProperty("database.connection")
    val databaseUsername: String = System.getProperty("database.username")
    val databasePassword: String = System.getProperty("database.password")
    val databasePort: Int = System.getProperty("ascient.port", "8080").toInt()

    Flyway
            .configure()
            .dataSource(databaseConnection, databaseUsername, databasePassword)
            .load()
            .migrate()

    Database.connect(
            databaseConnection,
            driver = "org.postgresql.Driver",
            user = databaseUsername,
            password = databasePassword)

    embeddedServer(
        Netty,
        port = databasePort) {
        server()
    }.start(wait = true)
}

fun Application.server() {
    install(ContentNegotiation) { jackson {
        registerModule(JodaModule())
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    } }

    install(StatusPages) {
        exception<MissingParam> {
            call.respond(HttpStatusCode.BadRequest, it.message ?: "")
        }

        exception<IllegalArgumentException> {
            call.respond(HttpStatusCode.BadRequest, it.message ?: "")
        }
    }

    routing {
        route("/api") {
            booleanRoutes()
        }
    }
}

class MissingParam(name: String) : IllegalArgumentException("Missing parameter: $name")
