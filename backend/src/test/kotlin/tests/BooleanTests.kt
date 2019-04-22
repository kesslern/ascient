package us.kesslern.ascient.tests

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import requests.*
import us.kesslern.ascient.BooleanDBO
import us.kesslern.ascient.TestContext
import us.kesslern.ascient.readJson
import us.kesslern.ascient.request
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
            assertEquals(HttpStatusCode.BadRequest, status)
            assertEquals("Missing parameter: name", content)
        }

        insertBoolean { id ->
            updateBoolean(id, null) {
                assertEquals(HttpStatusCode.BadRequest, status)
                assertEquals("'null' for field 'value' is invalid", content)
            }
        }

        insertBoolean { id ->
            request(HttpMethod.Put, "/api/booleans/$id", "{}") {
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }

        insertBoolean { id ->
            request(HttpMethod.Put, "/api/booleans/$id", "") {
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }
    }
}
