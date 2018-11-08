package us.kesslern.ascient

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Booleans : Table("booleans") {
    val id: Column<Int> = integer("id").autoIncrement().primaryKey()
    val name: Column<String> = varchar("name", 25)
    val value: Column<Boolean> = bool("value")
}

object StatusOk {
    val status: String = "OK"
}

data class AscientBoolean(
        val id: Int,
        val name: String,
        val value: Boolean
)

val databaseConnection: String = System.getProperty("database.connection")
val databaseUsername: String = System.getProperty("database.username")
val databasePassword: String = System.getProperty("database.password")

class Test(
        val val1: String,
        val val3: Boolean?
)

fun main(args: Array<String>) {
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

        install(ContentNegotiation) { jackson {} }

        routing {
            get("/api/test") {
                call.respond(
                        Test("It was...",
                                transaction {
                                    Booleans.select {
                                        Booleans.name eq "test"
                                    }.firstOrNull()?.get(Booleans.value)
                                }
                        )
                )
            }

            get("/api/booleans") {
                call.respond(
                        transaction {
                            Booleans.selectAll().map { it ->
                                AscientBoolean(
                                        it[Booleans.id],
                                        it[Booleans.name],
                                        it[Booleans.value]
                                )
                            }
                        }
                )
            }

            post("/api/booleans") {
                val newName = call.request.queryParameters["name"] ?: throw IllegalArgumentException()
                val newValue = when(call.request.queryParameters["value"]) {
                    "true" -> true
                    "false" -> false
                    else -> throw IllegalArgumentException()
                }

                val id = transaction {
                    Booleans.insert {
                        it[name] = newName
                        it[value] = newValue
                    } get(Booleans.id)
                }

                if (id == null) {
                    throw Exception()
                } else {
                    call.respond(id)
                }
            }

            put("/api/booleans/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException()
                val newValue = when (call.request.queryParameters["value"]) {
                    "true" -> true
                    "false" -> false
                    else -> throw IllegalArgumentException()
                }

                transaction {
                    Booleans.update({ Booleans.id eq id }) { it ->
                        it[value] = newValue
                    }
                }

                call.respond(StatusOk)
            }

            delete("/api/booleans/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException()

                transaction {
                    Booleans.deleteWhere { Booleans.id eq id }
                }

                call.respond(StatusOk)
            }
        }
    }.start(wait = true)
}
