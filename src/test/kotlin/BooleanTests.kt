package us.kesslern.ascient

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpStatusCode
import org.joda.time.DateTime
import org.joda.time.Seconds.THREE
import org.joda.time.Seconds.secondsBetween
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.test.*

@ExperimentalContracts
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BooleanTests {

    private val mapper = TestContext.mapper

    @Test
    fun `test no auth`() {
        getBooleans(authenticated = false) {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `test boolean CRUD operations`() {
        // insert new boolean with random UUID as name and record the new ID
        val name = UUID.randomUUID().toString()
        var newId: Int
        createBoolean(name, true) {
            assertEquals(HttpStatusCode.OK, status)
            val content = content
            assertNotNull(content)
            newId = content.toInt()
        }

        // verify boolean inserted with value and ID
        var modifiedAt: DateTime
        getBoolean(newId) {
            val currentTime = DateTime()
            val newBoolean = mapper.readValue(content, BooleanDBO::class.java)
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(newBoolean.value)
            assertTrue(secondsBetween(currentTime, newBoolean.creationTime) < THREE)
            modifiedAt = newBoolean.updatedAt
        }

        // change value to false
        updateBoolean(newId, false) {
            assertEquals(HttpStatusCode.NoContent, status)
        }

        // get all values and verify
        getBooleans {
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
        deleteBoolean(newId) {
            assertEquals(HttpStatusCode.NoContent, status)
        }

        // get all values and verify
        getBooleans {
            val newBooleans: List<BooleanDBO> = mapper.readValue(content ?: throw RuntimeException())
            assertEquals(HttpStatusCode.OK, status)
            assertNull(newBooleans.find { it.id == newId })
        }
    }

    @Test
    fun `test default parameters`() {
        insertBoolean(UUID.randomUUID().toString()) { id ->
            // Verify inserted with value True
            getBoolean(id) {
                val newBoolean = mapper.readValue(content, BooleanDBO::class.java)
                assertEquals(HttpStatusCode.OK, status)
                assertTrue(newBoolean.value)
            }
        }
    }

    @Test
    fun `test bad parameters`() {
        getBoolean(9999999) {
            assertEquals(HttpStatusCode.BadRequest, status)
        }

        getBoolean(-1) {
            assertEquals(HttpStatusCode.BadRequest, status)
        }

        createBoolean(null) {
            assertEquals("Missing parameter: name", content)
            assertEquals(HttpStatusCode.BadRequest, status)
        }


        insertBoolean(UUID.randomUUID().toString()) { id ->
            updateBoolean(id, null) {
                assertEquals("Missing parameter: value", content)
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }
    }

    private fun insertBoolean(name: String, value: Boolean? = null, block: (id: Int) -> Unit) {

        var id: Int
        createBoolean(name, value) {
            assertNotNull(content)
            id = content.toInt()
        }

        block(id)

        deleteBoolean(id) { }
    }
}
