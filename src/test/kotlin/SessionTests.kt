package us.kesslern.ascient

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import mu.KotlinLogging
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExperimentalContracts
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SessionTests {
    private val logger = KotlinLogging.logger {}

    @Test
    fun `create and use a session`() {
        var sessionId: String
        request(HttpMethod.Post, "/api/users/authenticate?username=admin&password=password") {
            assertEquals(HttpStatusCode.OK, status)
            assertNotNull(content)
            sessionId = content
        }
        logger.debug("Using session $sessionId")
        request(HttpMethod.Get, "/api/booleans", authenticated = false, sessionId = sessionId) {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `test empty session`() {
        request(HttpMethod.Get, "/api/booleans", authenticated = false, sessionId = "") {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `test unknown user`() {
        request(HttpMethod.Post, "/api/users/authenticate?username=baduser&password=password") {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `test bad password`() {
        request(HttpMethod.Post, "/api/users/authenticate?username=admin&password=foobar") {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }
}
