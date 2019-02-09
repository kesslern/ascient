package us.kesslern.ascient

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond

class AscientPrincipal : Principal

class AscientAuthenticationProvider(name: String?) : AuthenticationProvider(name) {
    internal var authenticationFunction: suspend ApplicationCall.(String?, String?) -> Principal? = { _, _ -> null }

    fun validate(body: suspend ApplicationCall.(String?, String?) -> Principal?) {
        authenticationFunction = body
    }
}


fun Authentication.Configuration.ascient(name: String? = null, configure: AscientAuthenticationProvider.() -> Unit) {
    val provider = AscientAuthenticationProvider(name).apply(configure)
    val authenticate = provider.authenticationFunction

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val authHeader = call.request.header("X-AscientAuth")
        val sessionHeader = call.request.header("X-AscientSession")
        val principal = with(call) { authenticate(authHeader, sessionHeader) }


        val error = when {
            authHeader == null && sessionHeader == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (error != null) {
            call.respond(HttpStatusCode.Unauthorized)
        }


        if (principal != null) {
            context.principal(principal)
        }
    }

    register(provider)
}
