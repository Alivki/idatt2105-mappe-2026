package com.iksystem.`ik-common`.session.repository

import com.iksystem.`ik-common`.session.model.Session
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * Spring Data JPA repository for [Session] entities.
 *
 * Provides finders for session lookup as well as bulk operations
 * for revoking and cleaning up sessions.
 */
interface SessionRepository : JpaRepository<Session, Long> {

    /**
     * Finds a session by its unique opaque [sessionId].
     *
     * @param sessionId The session identifier issued to the client.
     * @return The matching [Session], or `null` if none exists.
     */
    fun findBySessionId(sessionId: String): Session?

    /**
     * Returns all currently active sessions for a given user.
     *
     * @param userId The user's primary key.
     * @return A list of active [Session] entities.
     */
    fun findAllByUserIdAndActiveTrue(userId: Long): List<Session>

    /**
     * Deactivates every session belonging to the specified user.
     *
     * Uses a bulk JPQL UPDATE because Spring Data cannot derive
     * modification queries from method names alone.
     *
     * @param userId The user whose sessions should be deactivated.
     */
    @Modifying
    @Query("UPDATE Session s SET s.active = false WHERE s.user.id = :userId")
    fun deactivateAllByUserId(@Param("userId") userId: Long)

    /**
     * Deletes all sessions whose [Session.expiresAt] is in the past.
     *
     * Intended to be called by a scheduled cleanup job.
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < CURRENT_TIMESTAMP")
    fun deleteExpired()
}
