package us.kesslern.ascient

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

val databaseConnection: String = System.getProperty("database.connection")
val databaseUsername: String = System.getProperty("database.username")
val databasePassword: String = System.getProperty("database.password")

fun main() {
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
        port = 8080) {
        server()
    }.start(wait = true)
}

fun Application.server() {
    install(ContentNegotiation) { jackson {} }

    install(StatusPages) {
        exception<MissingParam> {
            call.respond(HttpStatusCode.BadRequest, it.message ?: "")
        }

        exception<IllegalArgumentException> {
            call.respond(HttpStatusCode.BadRequest, it.message ?: "")
        }
    }

    routing {
        get("/api/booleans") {
            call.respond(AscientBooleans.get())
        }

        get("/api/booleans/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw MissingParam("id")
            try {
                call.respond(AscientBooleans.get(id))
            } catch (e: NoSuchElementException) {
                throw IllegalArgumentException("Cannot locate boolean with ID $id")
            }
        }

        post("/api/booleans") {
            val newName = call.request.queryParameters["name"] ?: throw throw MissingParam("name")
            val newValue = call.request.queryParameters["value"]?.toBoolean() ?: throw MissingParam("value")

            val id = AscientBooleans.insert(newName, newValue)

            call.respond(id)
        }

        put("/api/booleans/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw MissingParam("id")
            val newValue = call.request.queryParameters["value"]?.toBoolean() ?: throw MissingParam("value")

            AscientBooleans.update(id, newValue)

            call.respond(HttpStatusCode.NoContent)
        }

        delete("/api/booleans/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw MissingParam("id")
            AscientBooleans.delete(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

class MissingParam(name: String) : IllegalArgumentException("Missing parameter: $name")
