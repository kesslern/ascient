package us.kesslern.ascient

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import java.util.*
import kotlin.concurrent.schedule

val sessions = AscientSessions(Environment.sessionLength)
val logger = KotlinLogging.logger {}

fun main() {
    Flyway
            .configure()
            .dataSource(Environment.databaseConnection, Environment.databaseUsername, Environment.databasePassword)
            .load()
            .migrate()

    Database.connect(
            Environment.databaseConnection,
            driver = "org.postgresql.Driver",
            user = Environment.databaseUsername,
            password = Environment.databasePassword)

    Timer().schedule(
            Environment.purgeInterval,
            Environment.purgeInterval
    ) {
        logger.info("Purging expired sessions...")
        sessions.purge()
    }

    embeddedServer(
            Netty,
            port = Environment.databasePort
    ) {
        server()
    }.start(wait = true)
}

fun Application.server() {
    val log = KotlinLogging.logger {}

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            registerModule(JodaModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        }
    }

    install(Authentication) {
        ascient {
            validate { authHeader, sessionHeader ->
                if (authHeader == Environment.password || sessions.check(sessionHeader)) {
                    AscientPrincipal()
                } else {
                    log.debug("Rejecting auth")
                    null
                }
            }
        }
    }

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
            userRoutes()
            booleanRoutes()
        }
    }
}

class MissingParam(name: String) : IllegalArgumentException("Missing parameter: $name")
