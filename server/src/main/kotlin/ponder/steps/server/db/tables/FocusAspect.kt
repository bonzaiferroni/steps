package ponder.steps.server.db.tables

import kabinet.utils.toInstantUtc
import klutch.db.Aspect
import klutch.utils.toStringId
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import ponder.steps.model.data.Focus

object FocusAspect: Aspect<FocusAspect, Focus>(
    TrekTable.join(StepTable, JoinType.LEFT, TrekTable.stepId, StepTable.id)
        .join(IntentTable, JoinType.LEFT, TrekTable.intentId, IntentTable.id),
    ResultRow::toFocus
) {
    val trekId = add(TrekTable.id)
    val intentLabel = add(IntentTable.label)
    val stepId = add(TrekTable.stepId)
    val stepLabel = add(StepTable.label)
    val stepIndex = add(TrekTable.stepIndex)
    val stepCount = add(TrekTable.stepCount)
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
    stepCount = this[FocusAspect.stepCount],
    stepPathSize = this[FocusAspect.stepPathSize],
    imgUrl = this[FocusAspect.imgUrl],
    startedAt = this[FocusAspect.startedAt]?.toInstantUtc()
)