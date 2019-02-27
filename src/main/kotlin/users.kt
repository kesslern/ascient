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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

data class UserDBO(
        val id: Int,
        val username: String,
        val password: String,
        val mustChangePassword: Boolean
)

object UsersTable : Table("users") {
    val id: Column<Int> = integer("id").autoIncrement().primaryKey()
    val username: Column<String> = varchar("username", 36)
    val password: Column<String> = varchar("password", 60)
    val mustChangePassword: Column<Boolean> = bool("must_change_password")
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
                        it[UsersTable.password],
                        it[UsersTable.mustChangePassword]
                    )
                } else null
            }.firstOrNull()
        }
    }

    fun updatePassword(id: Int, newPassword: String) {
        transaction {
            UsersTable.update({ UsersTable.id eq id }) {
                it[password] = newPassword
                it[mustChangePassword] = false
            }
        }
    }

    fun new(username: String, hash: String) {
        transaction {
            UsersTable.insert {
                it[UsersTable.username] = username
                it[UsersTable.password] = hash
            }
        }
    }
}

data class AuthenticationResponse(val sessionId: String, val mustChangePassword: Boolean)

fun Route.userRoutes() {
    route("/users") {
        post("/authenticate") {
            val username = call.request.queryParameters["username"] ?: throw MissingParam("username")
            val password = call.request.queryParameters["password"] ?: throw MissingParam("password")
            val user = UsersDAO.check(username, password)

            if (user != null) {
                call.respond(AuthenticationResponse(
                    sessionId = sessions.add(user),
                    mustChangePassword = user.mustChangePassword
                ))
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        post("/new") {
            val username = call.request.queryParameters["username"] ?: throw MissingParam("username")
            val password = call.request.queryParameters["password"] ?: throw MissingParam("password")
            val hash = BCrypt.hashpw(password, BCrypt.gensalt(12))
            UsersDAO.new(username, hash)
            call.respond(HttpStatusCode.NoContent)
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
