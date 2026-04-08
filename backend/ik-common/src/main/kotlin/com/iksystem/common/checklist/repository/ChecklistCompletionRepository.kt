package com.iksystem.common.checklist.repository

import com.iksystem.common.checklist.model.ChecklistCompletion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface ChecklistCompletionRepository : JpaRepository<ChecklistCompletion, Long> {

    @Query("SELECT COUNT(cc) FROM ChecklistCompletion cc WHERE cc.checklist.id = :checklistId AND cc.completedAt >= :from AND cc.completedAt < :to")
    fun countByChecklistIdAndCompletedAtBetween(checklistId: Long, from: Instant, to: Instant): Long

    fun deleteAllByChecklistId(checklistId: Long)
}
