package us.kesslern.ascient.util

import com.fasterxml.jackson.module.kotlin.readValue
import us.kesslern.ascient.TestContext

inline fun <reified T> readJson(content: String?): T {
    if (content == null) throw RuntimeException("Empty Content")
    return TestContext.mapper.readValue(content)
}

fun Any.toJson(): String = TestContext.mapper.writeValueAsString(this)
