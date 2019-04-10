package us.kesslern.ascient

import com.fasterxml.jackson.databind.ObjectMapper
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
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.exception.FlywaySqlException
import org.jetbrains.exposed.sql.Database
import java.io.EOFException
import java.net.ConnectException
import java.util.*
import kotlin.concurrent.schedule

val sessions = AscientSessions(Environment.sessionLength)
val logger = KotlinLogging.logger {}

fun main() {
    while (true) {
        try {
            Flyway
                    .configure()
                    .dataSource(Environment.databaseConnection, Environment.databaseUsername, Environment.databasePassword)
                    .load()
                    .migrate()
        } catch (e: FlywaySqlException) {
            val cause = e.cause?.cause
            if (cause is ConnectException || cause is EOFException) {
                logger.warn(e) { "Unable to connect to database. Waiting before retrying..." }
                Thread.sleep(10000)
                continue
            } else {
                throw e
            }
        }
        break
    }

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

var objectMapper: ObjectMapper? = null

@UseExperimental(ObsoleteCoroutinesApi::class)
fun Application.server() {
    val log = KotlinLogging.logger {}

    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)

    install(ContentNegotiation) {
        jackson {
            registerModule(JodaModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            objectMapper = this
        }
    }

    install(Authentication) {
        ascient {
            validate { sessionHeader, username, password ->

                val user = if (sessionHeader != null) {
                    sessions.check(sessionHeader)
                } else if (username != null && password != null) {
                    UsersDAO.check(username, password)
                } else null

                if (user != null) {
                    AscientPrincipal(user)
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
            webSocket("/websocket") {
                try {
                    val authFrame = incoming.receive() as Frame.Text
                    val user = sessions.check(authFrame.readText())
                    if (user == null) {
                        outgoing.send(Frame.Text("Unauthenticated"))
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Unauthenticated"))
                        return@webSocket
                    } else {
                        log.debug("Adding user ${user.id} to message broker")
                        MessageBroker.add(this, user)
                        outgoing.send(Frame.Text("Authenticated"))
                    }
                    incoming.receiveOrNull()
                } finally {
                    MessageBroker.remove(this)
                }
            }
            userRoutes()
            booleanRoutes()
        }
    }
}

class MissingParam(name: String) : IllegalArgumentException("Missing parameter: $name")
