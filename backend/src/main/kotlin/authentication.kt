package us.kesslern.ascient

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond

class AscientPrincipal(
    val user: UserDBO
) : Principal

class AscientAuthenticationProvider(name: String?) : AuthenticationProvider(name) {
    internal var authenticationFunction: suspend ApplicationCall.(String?, String?, String?) -> Principal? = { _, _, _ -> null }

    fun validate(body: suspend ApplicationCall.(String?, String?, String?) -> Principal?) {
        authenticationFunction = body
    }
}


fun Authentication.Configuration.ascient(name: String? = null, configure: AscientAuthenticationProvider.() -> Unit) {
    val provider = AscientAuthenticationProvider(name).apply(configure)
    val authenticate = provider.authenticationFunction

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val sessionHeader = call.request.header("X-AscientSession")
        val username = call.request.header("X-AscientUsername")
        val password = call.request.header("X-AscientPassword")
        val principal = with(call) { authenticate(sessionHeader, username, password) }

        val error = when {
           sessionHeader == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (error != null) {
            context.challenge("AscientAuth", error) {
                call.respond(HttpStatusCode.Unauthorized)
                it.complete()
            }
        }

        if (principal != null) {
            context.principal(principal)
        }
    }

    register(provider)
}
