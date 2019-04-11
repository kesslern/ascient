package us.kesslern.ascient

import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.*
import kotlin.concurrent.schedule

data class ActiveWebSocket(
        val session: WebSocketSession,
        val user: UserDBO,
        val sessionId: String
)

data class Event (
        val userId: Int,
        val action: String,
        val entity: Any,
        val originatingSessionId: String?
) {
    constructor(
            principal: AscientPrincipal,
            action: String,
            entity: Any
    ) : this(principal.user.id, action, entity, principal.sessionId)
}

data class WebSocketMessage(
        val type: String,
        val action: String,
        val entity: Any
)

object MessageBroker {
    val log = KotlinLogging.logger {}
    private val websockets = mutableListOf<ActiveWebSocket>()

    init {
        Timer().schedule(
                10000, 10000
        ) {
            logger.info("Current websockets: ${websockets.size}")
        }
    }

    fun add(
            session: WebSocketSession,
            user: UserDBO,
            sessionId: String
    ) = websockets.add(ActiveWebSocket(session, user, sessionId))

    fun remove(session: WebSocketSession) = websockets.removeIf { it.session == session }

    fun dispatch(event: Event) {
        val transport = WebSocketMessage(
                action = event.action,
                type = event.entity.javaClass.simpleName,
                entity = event.entity
        )

        websockets
                .filter {
                    it.user.id == event.userId && it.sessionId != event.originatingSessionId
                }.forEach { webSocket ->
                    GlobalScope.launch {
                        webSocket.session.send(objectMapper?.writeValueAsString(transport) ?: "")
                    }
                }
    }
}
