package us.kesslern.ascient

import io.ktor.application.call
import io.ktor.http.ContentType
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

fun main(args: Array<String>) {

    Database.connect(
            "jdbc:postgresql://localhost:5432/postgres?user=user&password=pass",
            driver = "org.postgresql.Driver")

    embeddedServer(Netty, 8080) {
        routing {
            get("/api/test") {

                var result: Boolean? = null
                transaction {
                    booleans.select {
                        booleans.name eq "test"
                    }.forEach { result = it[booleans.value] }
                }

                if (result != null) {
                    call.respondText("It was ${result}", ContentType.Text.Html)
                } else {
                    call.respondText("no bro", ContentType.Text.Html)
                }
            }
        }
    }.start(wait = true)
}