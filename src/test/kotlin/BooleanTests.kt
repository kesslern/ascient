package us.kesslern.ascient

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.joda.time.DateTime
import org.joda.time.Seconds.THREE
import org.joda.time.Seconds.secondsBetween
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BooleanTests {

    private val mapper = TestContext.mapper

    @Test
    fun `test boolean CRUD operations`() {
        // insert new boolean with random UUID as name and record the new ID
        val name = UUID.randomUUID()
        val newId = with(request(
                HttpMethod.Post,
                "/api/booleans?name=$name&value=${true}")) {
            assertEquals(HttpStatusCode.OK, status)
            val content = content
            assertNotNull(content)
            content.toInt()
        }

        // verify boolean inserted with value and ID
        var modifiedAt: DateTime
        with(request(HttpMethod.Get, "/api/booleans/$newId")) {
            val currentTime = DateTime()
            val newBoolean = mapper.readValue(content, BooleanDBO::class.java)
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(newBoolean.value)
            assertTrue(secondsBetween(currentTime, newBoolean.creationTime) < THREE)
            modifiedAt = newBoolean.updatedAt
        }

        // change value to false
        with(request(HttpMethod.Put, "/api/booleans/$newId?value=${false}")) {
            assertEquals(HttpStatusCode.NoContent, status)
        }

        // get all values and verify
        with(request(HttpMethod.Get, "/api/booleans")) {
            val content = content
            assertNotNull(content)
            val newBooleans: List<BooleanDBO> = mapper.readValue(content)
            assertEquals(HttpStatusCode.OK, status)
            val newBoolean = newBooleans.find { it.id == newId }
            assertNotNull(newBoolean)
            assertFalse(newBoolean.value)
            assertTrue(newBoolean.updatedAt.isAfter(modifiedAt))
        }

        // delete
        with(request(HttpMethod.Delete, "/api/booleans/$newId")) {
            assertEquals(HttpStatusCode.NoContent, status)
        }

        // get all values and verify
        with(request(HttpMethod.Get, "/api/booleans")) {
            val newBooleans: List<BooleanDBO> = mapper.readValue(content ?: throw RuntimeException())
            assertEquals(HttpStatusCode.OK, status)
            assertNull(newBooleans.find { it.id == newId })
        }
    }

    @Test
    fun `test default parameters`() {
        insertBoolean(UUID.randomUUID().toString()) { id ->
            // Verify inserted with value True
            with(request(HttpMethod.Get, "/api/booleans/$id")) {
                val newBoolean = mapper.readValue(content, BooleanDBO::class.java)
                assertEquals(HttpStatusCode.OK, status)
                assertTrue(newBoolean.value)
            }
        }
    }

    @Test
    fun `test bad parameters`() {
        with(request(HttpMethod.Get, "/api/booleans/9999999")) {
            assertEquals(HttpStatusCode.BadRequest, status)
        }

        with(request(HttpMethod.Get, "/api/booleans/-1")) {
            assertEquals(HttpStatusCode.BadRequest, status)
        }

        with(request(HttpMethod.Post, "/api/booleans")) {
            assertEquals("Missing parameter: name", content)
            assertEquals(HttpStatusCode.BadRequest, status)
        }


        insertBoolean(UUID.randomUUID().toString()) { id ->
            with(request(HttpMethod.Put, "/api/booleans/$id")) {
                assertEquals("Missing parameter: value", content)
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }
    }

    private fun insertBoolean(name: String, value: String? = null, block: (id: Int) -> Unit) {
        val uri = "/api/booleans?name=$name${if (value != null) "&value=$value" else ""}"

        val id = request(HttpMethod.Post, uri).content?.toInt()
                ?: throw AssertionError("Expected successful boolean insertion")

        block(id)

        request(HttpMethod.Delete, "/api/booleans/$id")
    }
}
