package us.kesslern.ascient.requests

import io.ktor.http.HttpMethod
import us.kesslern.ascient.util.QueryParamBase
import us.kesslern.ascient.util.UnifiedResponse
import us.kesslern.ascient.util.request
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class UserAuthenticateParams(
    val password: String? = null,
    val username: String? = null
) : QueryParamBase()

class UserCreateParams(
    val password: String? = null,
    val username: String? = null
) : QueryParamBase()

@ExperimentalContracts
fun authenticateUser(username: String, password: String, handler: UnifiedResponse.() -> Unit) {
    contract {
        callsInPlace(handler, InvocationKind.EXACTLY_ONCE)
    }
    val params = UserAuthenticateParams(
        username = username,
        password = password
    )
    request(
        HttpMethod.Post,
        "/api/users/authenticate$params",
        handler = handler
    )
}

@ExperimentalContracts
fun createUser(username: String, password: String, handler: UnifiedResponse.() -> Unit) {
    contract {
        callsInPlace(handler, InvocationKind.EXACTLY_ONCE)
    }
    val params = UserCreateParams(
        username = username,
        password = password
    )
    request(
        HttpMethod.Post,
        "/api/users/new$params",
        handler = handler
    )
}
