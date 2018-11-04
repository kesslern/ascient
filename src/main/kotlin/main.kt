package us.kesslern.ascient

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object booleans : Table() {
    val id: Column<Int> = integer("id").autoIncrement().primaryKey()
    val name: Column<String> = varchar("name", 25)
    val value: Column<Boolean> = bool("value")
}

class Test(
        val val1: String,
        val val2: Int,
        val val3: Boolean
)

fun main(args: Array<String>) {
    val databaseConnection = System.getProperty("database.connection")

    Database.connect(
            databaseConnection,
            driver = "org.postgresql.Driver")

    embeddedServer(Netty, 8080) {
        install(ContentNegotiation) {
            jackson {}
        }
        routing {
            get("/api/test") {

                var result: Boolean? = null
                transaction {
                    booleans.select {
                        booleans.name eq "test"
                    }.forEach { result = it[booleans.value] }
                }

                if (result != null) {
                    call.respond(Test("It was...", 123, result!!))
                } else {
                    call.respondText("no bro", ContentType.Text.Html)
                }
            }
        }
    }.start(wait = true)
}