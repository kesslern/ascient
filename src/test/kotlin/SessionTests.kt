package us.kesslern.ascient

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import mu.KotlinLogging
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SessionTests {
    private val logger = KotlinLogging.logger {}

    @Test
    fun `create and use a session`() {
        var sessionId: String
        request(HttpMethod.Post, "/api/users/authenticate?username=admin&password=password").run {
            assertEquals(HttpStatusCode.OK, status)
            assertNotNull(content)
            sessionId = content
        }
        logger.debug("Using session $sessionId")
        request(HttpMethod.Get, "/api/booleans", authenticated = false, sessionId = sessionId).run {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `test empty session`() {
        request(HttpMethod.Get, "/api/booleans", authenticated = false, sessionId = "").run {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `test unknown user`() {
        request(HttpMethod.Post, "/api/users/authenticate?username=baduser&password=password").run {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `test bad password`() {
        request(HttpMethod.Post, "/api/users/authenticate?username=admin&password=foobar").run {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }
}
