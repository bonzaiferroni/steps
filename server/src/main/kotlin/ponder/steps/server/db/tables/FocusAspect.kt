package ponder.steps.server.db.tables

import kabinet.utils.toInstantFromUtc
import klutch.db.Aspect
import klutch.utils.toStringId
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import ponder.steps.model.data.Focus

object FocusAspect: Aspect<FocusAspect, Focus>(
    TrekTable.join(StepTable, JoinType.LEFT, TrekTable.nextId, StepTable.id)
        .join(IntentTable, JoinType.LEFT, TrekTable.intentId, IntentTable.id),
    ResultRow::toFocus
) {
    val trekId = add(TrekTable.id)
    val intentLabel = add(IntentTable.label)
    val stepId = add(TrekTable.nextId)
    val stepLabel = add(StepTable.label)
    val stepIndex = add(TrekTable.progress)
    val stepPathSize = add(StepTable.pathSize)
    val imgUrl = add(StepTable.imgUrl)
    val startedAt = add(TrekTable.startedAt)
}

fun ResultRow.toFocus() = Focus(
    trekId = this[FocusAspect.trekId].value.toStringId(),
    stepId = this[FocusAspect.stepId].value.toStringId(),
    intentLabel = this[FocusAspect.intentLabel],
    stepLabel = this[FocusAspect.stepLabel],
    stepIndex = this[FocusAspect.stepIndex],
    stepPathSize = this[FocusAspect.stepPathSize],
    imgUrl = this[FocusAspect.imgUrl],
    startedAt = this[FocusAspect.startedAt]?.toInstantFromUtc()
)