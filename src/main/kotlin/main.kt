package us.kesslern.ascient

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object StatusOk {
    val status: String = "OK"
}

val databaseConnection: String = System.getProperty("database.connection")
val databaseUsername: String = System.getProperty("database.username")
val databasePassword: String = System.getProperty("database.password")
val useH2: Boolean = System.getProperty("database.h2")?.toBoolean() ?: false

class Test(
        val val1: String,
        val val3: Boolean?
)

fun Application.main() {
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

    embeddedServer(Netty, 8080) {
        server()
    }.start(wait = true)
}

fun Application.server() {
    install(ContentNegotiation) { jackson {} }

    routing {
        get("/api/booleans") {
            call.respond(AscientBooleans.get())
        }

        post("/api/booleans") {
            val newName = call.request.queryParameters["name"] ?: throw IllegalArgumentException()
            val newValue = call.request.queryParameters["value"]?.toBoolean() ?: throw IllegalArgumentException()

            val id = AscientBooleans.insert(newName, newValue)

            call.respond(id)
        }

        put("/api/booleans/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException()
            val newValue = call.request.queryParameters["value"]?.toBoolean() ?: throw IllegalArgumentException()

            AscientBooleans.update(id, newValue)

            call.respond(StatusOk)
        }

        delete("/api/booleans/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException()
            AscientBooleans.delete(id)
            call.respond(StatusOk)
        }
    }
}
