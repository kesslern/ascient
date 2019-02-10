package us.kesslern.ascient

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SessionTests {

    @Test
    fun `create and use a session`() {
        var sessionId: String
        request(HttpMethod.Post, "/authenticate").run {
            assertEquals(HttpStatusCode.OK, status)
            assertNotNull(content)
            sessionId = content
        }
        request(HttpMethod.Get, "/api/booleans", authenticated = false, sessionId = sessionId).run {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `test empty session`() {
        request(HttpMethod.Post, "/authenticate", authenticated = false, sessionId = "").run {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }
}
