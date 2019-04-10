package us.kesslern.ascient

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

data class ActiveWebSocket(
        val session: WebSocketSession,
        val user: UserDBO
)

object MessageBroker {
    private val websockets = mutableListOf<ActiveWebSocket>()

    init {
        Timer().schedule(
                3000, 3000
        ) {
            logger.info("Current websockets: ${websockets.size}")
            websockets.forEach { websocket ->
                GlobalScope.launch { websocket.session.outgoing.send(Frame.Text("I still see you")) }
            }
        }
    }

    fun add(session: WebSocketSession, user: UserDBO) = websockets.add(ActiveWebSocket(session, user))

    fun remove(session: WebSocketSession) = websockets.removeIf { it.session == session }
}