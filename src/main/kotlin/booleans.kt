import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import us.kesslern.ascient.MissingParam

data class BooleanDBO(
        val id: Int,
        val name: String,
        val value: Boolean
)

object BooleansTable : Table("booleans") {
    val id: Column<Int> = integer("id").autoIncrement().primaryKey()
    val name: Column<String> = varchar("name", 36)
    val value: Column<Boolean> = bool("value")
}

object BooleansDAO {
    fun get(): List<BooleanDBO> =
            transaction {
                BooleansTable.selectAll().map { it ->
                    BooleanDBO(
                            it[BooleansTable.id],
                            it[BooleansTable.name],
                            it[BooleansTable.value]
                    )
                }
            }

    fun get(id: Int): BooleanDBO =
        transaction {
            BooleansTable.select { BooleansTable.id eq id}.map { it ->
                BooleanDBO(
                        it[BooleansTable.id],
                        it[BooleansTable.name],
                        it[BooleansTable.value]
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
    route("/booleans") {
        get {
            call.respond(BooleansDAO.get())
        }

        post {
            val newName = call.request.queryParameters["name"] ?: throw throw MissingParam("name")
            val newValue = call.request.queryParameters["value"]?.toBoolean() ?: true

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
