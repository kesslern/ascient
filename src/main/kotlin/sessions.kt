package us.kesslern.ascient

import mu.KotlinLogging
import java.util.*

class SessionExistsException(id: String) : Exception() {
    private val logger = KotlinLogging.logger {}

    init {
        logger.info("Session already exists with ID: $id")
    }
}

data class AscientSession(
        val id: String,
        val user: UserDBO,
        var lastActive: Date = Date()
)

fun MutableList<AscientSession>.findById(id: String) = this.find { it.id == id }


class AscientSessions(
        /**
         * Inactivity time (in seconds) before sessions are purged.
         */
        private val sessionLength: Int
) {
    private val sessions: MutableList<AscientSession> = ArrayList()
    private val logger = KotlinLogging.logger {}

    fun add(user: UserDBO): String {
        val uuid = UUID.randomUUID().toString()
        sessions.add(AscientSession(uuid, user))
        return uuid
    }

    fun check(id: String?): UserDBO? {
        if (id == null) return null
        val session = sessions.findById(id)

        if (session == null) {
            logger.debug("No session: $id")
            return null
        }

        val now = Date()

        return if (now.before(session.expiration())) {
            logger.debug("Updating last active time of session $id")
            session.lastActive = now
            session.user
        } else null
    }

    fun purge() {
        val now = Date()
        sessions.removeIf {
            if (it.expiration().before(now)) {
                logger.debug("Removing session: ${it.id}")
                true
            } else false
        }
    }

    private fun AscientSession.expiration() = Date(this.lastActive.time + sessionLength * 1000)
}