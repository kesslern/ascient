package us.kesslern.ascient

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
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

data class BooleanPutBody(
        val value: Boolean
)

data class BooleanPostBody(
        val name: String?,
        val value: Boolean?
)

fun Route.booleanRoutes() {
    authenticate {
        route("/booleans") {
            get {
                call.respond(BooleansDAO.get())
            }

            get("/{id}") {
                val id = call.pathIntParam("id")
                try {
                    call.respond(BooleansDAO.get(id))
                } catch (e: NoSuchElementException) {
                    throw IllegalArgumentException("Cannot locate boolean with ID $id")
                }
            }

            post {
                val principal = call.ascientPrincipal()
                val body = call.receive<BooleanPostBody>()
                if (body.name?.isEmpty() != false) { throw MissingParam("name") }

                val boolean = BooleansDAO.insert(body.name, body.value ?: true)

                MessageBroker.dispatch(Event(principal, "SET", boolean))
                call.respond(boolean)
            }

            put("/{id}") {

                val principal = call.ascientPrincipal()
                val id = call.pathIntParam("id")
                val body = call.receive<BooleanPutBody>()

                val boolean = BooleansDAO.update(id, body.value)

                MessageBroker.dispatch(Event(principal, "SET", boolean))
                call.respond(boolean)
            }

            delete("/{id}") {
                val principal = call.ascientPrincipal()
                val id = call.pathIntParam("id")
                BooleansDAO.delete(id)
                MessageBroker.dispatch(Event(principal, "DELETE", id))
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

