package us.kesslern.ascient

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
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

    fun insert(newName: String, newValue: Boolean): Int =
            transaction {
                BooleansTable.insert {
                    it[name] = newName
                    it[value] = newValue
                } get (BooleansTable.id) ?: throw IllegalArgumentException()
            }

    fun update(id: Int, newValue: Boolean) {
        transaction {
            BooleansTable.update({ BooleansTable.id eq id }) {
                it[value] = newValue
            }
        }
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

                val id = BooleansDAO.insert(newName, newValue)

                call.respond(id)
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
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("this won't happen...")
                val newValue = call.request.queryParameters["value"]?.toBoolean() ?: throw MissingParam("value")

                BooleansDAO.update(id, newValue)

                call.respond(HttpStatusCode.NoContent)
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw MissingParam("id")
                BooleansDAO.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
