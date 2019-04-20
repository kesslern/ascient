package us.kesslern.ascient

import io.ktor.http.HttpMethod
import requests.BooleanCreateParams
import requests.BooleanUpdateParams
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalContracts
fun authenticateUser(username: String, password: String, handler: UnifiedResponse.() -> Unit) {
    contract {
        callsInPlace(handler, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    request(
            HttpMethod.Post,
            "/api/users/authenticate?username=$username&password=$password",
            handler = handler
    )
}

@ExperimentalContracts
fun newUser(username: String, password: String, handler: UnifiedResponse.() -> Unit) {
    contract {
        callsInPlace(handler, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    request(
        HttpMethod.Post,
        "/api/users/new?username=$username&password=$password",
        handler = handler
    )
}

@ExperimentalContracts
fun getBoolean(
        id: Int,
        authenticated: Boolean = true,
        sessionId: String? = null,
        handler: UnifiedResponse.() -> Unit
) {
    contract {
        callsInPlace(handler, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
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
        callsInPlace(handler, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
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
        callsInPlace(handler, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
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
        callsInPlace(handler, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
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
        callsInPlace(handler, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
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
