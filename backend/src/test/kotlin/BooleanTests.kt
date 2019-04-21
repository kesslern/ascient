package us.kesslern.ascient

import io.ktor.http.HttpStatusCode
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
        var boolean: BooleanDBO
        createBoolean(name, true) {
            assertEquals(HttpStatusCode.OK, status)
            boolean = readJson(content)
        }

        // verify boolean inserted with value and ID
        getBoolean(boolean.id) {
            val newBoolean: BooleanDBO = readJson(content)
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(newBoolean.value)
            assertEquals(boolean.creationTime, newBoolean.creationTime)
            assertEquals(boolean.updatedAt, newBoolean.updatedAt)
        }

        // change value to false
        updateBoolean(boolean.id, false) {
            val newBoolean: BooleanDBO = readJson(content)
            assertEquals(HttpStatusCode.OK, status)
            assertFalse(newBoolean.value)
        }

        // get all values and verify
        getBooleans {
            val newBooleans: List<BooleanDBO> = readJson(content)
            assertEquals(HttpStatusCode.OK, status)
            val newBoolean = newBooleans.find { it.id == boolean.id }
            assertNotNull(newBoolean)
            assertFalse(newBoolean.value)
            assertTrue(newBoolean.updatedAt.isAfter(boolean.updatedAt))
        }

        // delete
        deleteBoolean(boolean.id) {
            assertEquals(HttpStatusCode.NoContent, status)
        }

        // get all values and verify
        getBooleans {
            val newBooleans: List<BooleanDBO> = readJson(content)
            assertEquals(HttpStatusCode.OK, status)
            assertNull(newBooleans.find { it.id == boolean.id })
        }
    }

    @Test
    fun `test default parameters`() {
        insertBoolean { id ->
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

        insertBoolean { id ->
            updateBoolean(id, null) {
                assertEquals("Missing parameter: value", content)
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }
    }

    private fun insertBoolean(name: String = UUID.randomUUID().toString(),
                              value: Boolean? = null,
                              handler: (id: Int) -> Unit) {
        var id: Int
        createBoolean(name, value) {
            val boolean: BooleanDBO = readJson(content)
            id = boolean.id
        }

        handler(id)

        deleteBoolean(id)
    }
}
