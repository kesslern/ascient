package us.kesslern.ascient

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt

data class UsersDBO(
        val id: Int,
        val username: String,
        val password: String
)

object UsersTable : Table("users") {
    val id: Column<Int> = integer("id").autoIncrement().primaryKey()
    val username: Column<String> = varchar("username", 36)
    val password: Column<String> = varchar("password", 60)
}

object UsersDAO {
    private val log = KotlinLogging.logger {}

    fun check(username: String, password: String): Boolean {
        return transaction {
            UsersTable.select { UsersTable.username eq username }.map {
                val databasePassword = it[UsersTable.password]
                log.debug("Checking username $username")
                BCrypt.checkpw(password, databasePassword)
            }.first()
        }
    }
}

fun Route.userRoutes() {
    route("/users") {
        post("/authenticate") {
            val username = call.request.queryParameters["username"] ?: throw MissingParam("username")
            val password = call.request.queryParameters["password"] ?: throw MissingParam("password")

            val hash = password

            call.respond(UsersDAO.check(username, hash))
        }
    }
}