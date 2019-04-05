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
import io.ktor.http.cio.websocket.*
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.mapNotNull
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import java.time.Duration
import java.util.*
import kotlin.concurrent.schedule

data class AscientWebsocketSession(val webSocketSession: WebSocketSession, val callback: suspend () -> Unit)

val sessions = AscientSessions(Environment.sessionLength)
val logger = KotlinLogging.logger {}

val activeWebSockets = mutableListOf<AscientWebsocketSession>()

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

    Timer().schedule(
            3000, 3000
    ) {
        logger.info("active web sockets: {}", activeWebSockets.size)
        activeWebSockets.forEach {
            GlobalScope.launch {
                it.callback()
            }
        }
    }

    embeddedServer(
            Netty,
            port = Environment.databasePort
    ) {
        server()
    }.start(wait = true)
}

@ObsoleteCoroutinesApi
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

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
        timeout = Duration.ofSeconds(15)
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

    log.info("Starting...")
    routing {
        route("/api") {
            webSocket("/websocket") {
                log.info("New websocket")
                val sendit = suspend {
                    outgoing.send(Frame.Text("ping"))
                }
                val thing = AscientWebsocketSession(this, sendit)
                activeWebSockets.add(thing)
                incoming.mapNotNull { it as? Frame.Text }.consumeEach { frame ->
                    val text = frame.readText()
                    log.info("Websocket: $text")
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.trim().equals("bye", ignoreCase = true)) {
                        log.info("Closing websocket...")
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
                activeWebSockets.remove(thing)
            }
            userRoutes()
            booleanRoutes()
        }
    }
}

class MissingParam(name: String) : IllegalArgumentException("Missing parameter: $name")
