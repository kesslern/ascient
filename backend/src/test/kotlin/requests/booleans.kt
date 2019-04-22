package us.kesslern.ascient.requests

import io.ktor.http.HttpMethod
import us.kesslern.ascient.BooleanDBO
import us.kesslern.ascient.util.UnifiedResponse
import us.kesslern.ascient.util.readJson
import us.kesslern.ascient.util.request
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class BooleanCreateParams(
    val name: String?,
    val value: Boolean?
)

class BooleanUpdateParams(
    val value: Boolean? = null
)

@ExperimentalContracts
fun getBoolean(
    id: Int,
    authenticated: Boolean = true,
    sessionId: String? = null,
    handler: UnifiedResponse.() -> Unit
) {
    contract {
        callsInPlace(handler, InvocationKind.EXACTLY_ONCE)
    }
    request(HttpMethod.Get, "/api/booleans/$id", null, authenticated, sessionId, handler)
}

@ExperimentalContracts
fun deleteBoolean(
    id: Int,
    authenticated: Boolean = true,
    sessionId: String? = null,
    handler: UnifiedResponse.() -> Unit = {}
) {
    contract {
        callsInPlace(handler, InvocationKind.EXACTLY_ONCE)
    }
    request(HttpMethod.Delete, "/api/booleans/$id", null, authenticated, sessionId, handler)
}

@ExperimentalContracts
fun updateBoolean(
    id: Int,
    value: Boolean?,
    authenticated: Boolean = true,
    sessionId: String? = null,
    handler: UnifiedResponse.() -> Unit
) {
    contract {
        callsInPlace(handler, InvocationKind.EXACTLY_ONCE)
    }
    val params = BooleanUpdateParams(value = value)
    request(HttpMethod.Put, "/api/booleans/$id", params, authenticated, sessionId, handler)
}

@ExperimentalContracts
fun getBooleans(
    authenticated: Boolean = true,
    sessionId: String? = null,
    handler: UnifiedResponse.() -> Unit
) {
    contract {
        callsInPlace(handler, InvocationKind.EXACTLY_ONCE)
    }
    request(HttpMethod.Get, "/api/booleans", null, authenticated, sessionId, handler)
}

@ExperimentalContracts
fun createBoolean(
    name: String?,
    value: Boolean? = null,
    authenticated: Boolean = true,
    sessionId: String? = null,
    handler: UnifiedResponse.() -> Unit
) {
    contract {
        callsInPlace(handler, InvocationKind.EXACTLY_ONCE)
    }
    val params = BooleanCreateParams(
            name = name,
            value = value
    )
    request(
        HttpMethod.Post,
            "/api/booleans",
            params,
            authenticated,
            sessionId,
            handler
    )
}

@ExperimentalContracts
fun insertBoolean(name: String = UUID.randomUUID().toString(),
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
