package us.kesslern.ascient

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

data class BooleanDBO(
        val id: Int,
        val name: String,
        val value: Boolean,
        val creationTime: DateTime,
        val updatedAt: DateTime
)

object BooleansTable : Table("booleans") {
    val id: Column<Int> = integer("id").autoIncrement().primaryKey()
    val name: Column<String> = varchar("name", 36)
    val value: Column<Boolean> = bool("value")
    val creationDate: Column<DateTime> = datetime("creation_date")
    val updatedAt: Column<DateTime> = datetime("updated_at")
}

object BooleansDAO {
    fun get(): List<BooleanDBO> =
            transaction {
                BooleansTable.selectAll().orderBy(BooleansTable.id to true).map {
                    BooleanDBO(
                            it[BooleansTable.id],
                            it[BooleansTable.name],
                            it[BooleansTable.value],
                            it[BooleansTable.creationDate],
                            it[BooleansTable.updatedAt]
                    )
                }
            }

    fun get(id: Int): BooleanDBO =
            transaction {
                BooleansTable.select { BooleansTable.id eq id }.map {
                    BooleanDBO(
                            it[BooleansTable.id],
                            it[BooleansTable.name],
                            it[BooleansTable.value],
                            it[BooleansTable.creationDate],
                            it[BooleansTable.updatedAt]
                    )
                }.first()
            }

    fun insert(newName: String, newValue: Boolean): BooleanDBO =
            transaction {
                val id = BooleansTable.insert {
                    it[name] = newName
                    it[value] = newValue
                } get (BooleansTable.id) ?: throw IllegalArgumentException()
                get(id)
            }

    fun update(id: Int, newValue: Boolean): BooleanDBO =
        transaction {
            BooleansTable.update({ BooleansTable.id eq id }) {
                it[value] = newValue
            }
            get(id)
        }

    fun delete(id: Int) {
        transaction {
            BooleansTable.deleteWhere { BooleansTable.id eq id }
        }
    }
}

fun Route.booleanRoutes() {
    authenticate {
        route("/booleans") {
            get {
                call.respond(BooleansDAO.get())
            }

            post {
                val newName = call.request.queryParameters["name"] ?: throw MissingParam("name")
                val newValue = call.request.queryParameters["value"]?.toBoolean() ?: true

                if (newName.isEmpty()) { throw MissingParam("name") }

                val boolean = BooleansDAO.insert(newName, newValue)

                call.respond(boolean)
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw MissingParam("id")
                try {
                    call.respond(BooleansDAO.get(id))
                } catch (e: NoSuchElementException) {
                    throw IllegalArgumentException("Cannot locate boolean with ID $id")
                }
            }

            put("/{id}") {
                val principal = call.principal<AscientPrincipal>()!!
                val id = call.parameters["id"]?.toInt()!!
                val newValue = call.request.queryParameters["value"]?.toBoolean() ?: throw MissingParam("value")

                val boolean = BooleansDAO.update(id, newValue)

                MessageBroker.dispatch(Event(principal.user.id, boolean, principal.sessionId))
                call.respond(boolean)
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw MissingParam("id")
                BooleansDAO.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
