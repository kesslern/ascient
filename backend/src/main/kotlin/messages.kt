package us.kesslern.ascient

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.*
import kotlin.concurrent.schedule

data class ActiveWebSocket(
        val session: WebSocketSession,
        val user: UserDBO
)

data class Event (
        val userId: Int,
        val entity: Any
)

data class WebSocketTransport(
        val userId: Int,
        @JsonProperty("type")
        val type: String,
        val entity: Any
)

object MessageBroker {
    val log = KotlinLogging.logger {}
    private val websockets = mutableListOf<ActiveWebSocket>()

    init {
        Timer().schedule(
                3000, 3000
        ) {
            logger.info("Current websockets: ${websockets.size}")
        }
    }

    fun add(session: WebSocketSession, user: UserDBO) = websockets.add(ActiveWebSocket(session, user))

    fun remove(session: WebSocketSession) = websockets.removeIf { it.session == session }

    fun dispatch(event: Event) {
        val transport = WebSocketTransport(
                userId = event.userId,
                type = event.entity.javaClass.simpleName,
                entity = event.entity
        )
        log.info(objectMapper?.writeValueAsString(transport) ?: "")
        websockets.filter { it.user.id == event.userId }.forEach { webSocket ->
            GlobalScope.launch { webSocket.session.send(objectMapper?.writeValueAsString(transport) ?: "") }
        }
    }
}
