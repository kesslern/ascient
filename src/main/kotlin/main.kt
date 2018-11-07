package us.kesslern.ascient

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
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

            get("/api/booleans/create") {
                transaction {
                    Booleans.insert {
                        it[name] = call.request.queryParameters["name"] ?: throw IllegalArgumentException()
                        it[value] = true
                    }
                }

                call.respond(StatusOk)
            }

            get("/api/booleans/update") {
                val name = call.request.queryParameters["name"] ?: throw IllegalArgumentException()
                val newValue = when (call.request.queryParameters["value"]) {
                    "true" -> true
                    "false" -> false
                    else -> throw IllegalArgumentException()
                }

                transaction {
                    Booleans.update({ Booleans.name eq name }) { it ->
                        it[value] = newValue
                    }
                }

                call.respond(StatusOk)
            }

            get("/api/booleans/delete") {
                val name = call.request.queryParameters["name"] ?: throw IllegalArgumentException()

                transaction {
                    Booleans.deleteWhere { Booleans.name eq name }
                }

                call.respond(StatusOk)
            }
        }
    }.start(wait = true)
}
