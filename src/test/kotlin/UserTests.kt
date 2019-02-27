package us.kesslern.ascient

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import java.time.Instant
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalContracts
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserTests {
    @Test
    fun `verify admin user password must be changed flag`() {
        var sessionId: String
        authenticateUser("admin", "password") {
            assertEquals(HttpStatusCode.OK, status)
            val response: AuthenticationResponse = readJson(content)
            assertTrue(response.mustChangePassword)
            sessionId = response.sessionId
        }

        request(HttpMethod.Post, "/api/users/change-password?password=password", authenticated = false, sessionId = sessionId) {
            assertEquals(HttpStatusCode.NoContent, status)
        }

        authenticateUser("admin", "password") {
            assertEquals(HttpStatusCode.OK, status)
            val response: AuthenticationResponse = readJson(content)
            assertFalse(response.mustChangePassword)
        }
    }

    @Test
    fun `create and authenticate as a new user`() {
        val username = "user${Instant.now().toEpochMilli()}"
        newUser(username, "password") {
            assertEquals(HttpStatusCode.NoContent, status)
        }

        authenticateUser(username, "password") {
            assertEquals(HttpStatusCode.OK, status)
            val response: AuthenticationResponse = readJson(content)
            assertFalse(response.mustChangePassword)
        }
    }
}
