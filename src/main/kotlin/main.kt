package us.kesslern.ascient

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import java.util.*

val validSessions = mutableListOf<String>()

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

class AscientPrincipal : Principal

class AscientAuthenticationProvider(name: String?) : AuthenticationProvider(name) {
    internal var authenticationFunction: suspend ApplicationCall.(String?, String?) -> Principal? = { _, _ -> null }

    fun validate(body: suspend ApplicationCall.(String?, String?) -> Principal?) {
        authenticationFunction = body
    }
}


fun Authentication.Configuration.ascient(name: String? = null, configure: AscientAuthenticationProvider.() -> Unit) {
    val provider = AscientAuthenticationProvider(name).apply(configure)
    val authenticate = provider.authenticationFunction

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val authHeader = call.request.header("X-AscientAuth")
        val sessionHeader = call.request.header("X-AscientSession")
        val principal = with(call) { authenticate(authHeader, sessionHeader) }


        val error = when {
            authHeader == null && sessionHeader == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (error != null) {
            call.respond(HttpStatusCode.Unauthorized)
        }


        if (principal != null) {
            context.principal(principal)
        }
    }

    register(provider)
}

fun Application.server() {
    install(ContentNegotiation) {
        jackson {
            registerModule(JodaModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        }
    }

    install(Authentication) {
        ascient {
            validate { authHeader, sessionHeader ->
                if (authHeader == "please" || validSessions.contains(sessionHeader)) {
                    AscientPrincipal()
                } else {
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
        authenticate {
            route("/api") {
                booleanRoutes()
            }
            route("/authenticate") {
                post {
                    val sessionId = UUID.randomUUID().toString()
                    validSessions.add(sessionId)
                    call.respond(sessionId)
                }
            }
        }
    }
}

class MissingParam(name: String) : IllegalArgumentException("Missing parameter: $name")
