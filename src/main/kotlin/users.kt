package us.kesslern.ascient

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt

data class UserDBO(
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

    fun check(username: String, password: String): UserDBO? {
        return transaction {
            UsersTable.select { UsersTable.username eq username }.map {
                val databasePassword = it[UsersTable.password]
                log.debug("Checking username $username")
                if (BCrypt.checkpw(password, databasePassword)) {
                    UserDBO(
                        it[UsersTable.id],
                        it[UsersTable.username],
                        it[UsersTable.password]
                    )
                } else null
            }.firstOrNull()
        }
    }

    fun updatePassword(id: Int, newPassword: String) {
        transaction {
            UsersTable.update({ UsersTable.id eq id }) {
                it[password] = newPassword
            }
        }
    }
}

fun Route.userRoutes() {
    route("/users") {
        post("/authenticate") {
            val username = call.request.queryParameters["username"] ?: throw MissingParam("username")
            val password = call.request.queryParameters["password"] ?: throw MissingParam("password")
            val user = UsersDAO.check(username, password)

            if (user != null) {
                call.respond(sessions.add(user))
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        authenticate {
            post("/change-password") {
                val password = call.request.queryParameters["password"] ?: throw MissingParam("password")
                val principal: AscientPrincipal = call.authentication.principal()!!
                val hash = BCrypt.hashpw(password, BCrypt.gensalt(12))
                UsersDAO.updatePassword(principal.user.id, hash)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
