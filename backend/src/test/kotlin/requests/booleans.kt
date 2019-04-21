package requests

import io.ktor.http.HttpMethod
import us.kesslern.ascient.BooleanDBO
import us.kesslern.ascient.UnifiedResponse
import us.kesslern.ascient.readJson
import us.kesslern.ascient.request
import us.kesslern.ascient.util.QueryParamBase
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class BooleanCreateParams(
    val name: String? = null,
    val value: String? = null
) : QueryParamBase()

class BooleanUpdateParams(
    val value: String? = null
) : QueryParamBase()

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
    request(HttpMethod.Get, "/api/booleans/$id", authenticated, sessionId, handler)
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
    request(HttpMethod.Delete, "/api/booleans/$id", authenticated, sessionId, handler)
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
    val params = BooleanUpdateParams(value = value?.toString())
    request(HttpMethod.Put, "/api/booleans/$id$params", authenticated, sessionId, handler)
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
    request(HttpMethod.Get, "/api/booleans", authenticated, sessionId, handler)
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
        value = value?.toString()
    )
    request(
        HttpMethod.Post,
        "/api/booleans$params",
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
