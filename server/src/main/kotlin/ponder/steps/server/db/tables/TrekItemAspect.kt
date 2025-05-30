package ponder.steps.server.db.tables

import klutch.db.Aspect
import klutch.utils.toInstantUtc
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import ponder.steps.model.data.TrekItem

internal object TrekItemAspect: Aspect<TrekItemAspect, TrekItem>(
    TrekTable.join(StepTable, JoinType.LEFT, TrekTable.stepId, StepTable.id)
        .leftJoin(IntentTable),
    ResultRow::toTrekItem
) {
    val trekId = add(TrekTable.id)
    val stepLabel = add(StepTable.label)
    val stepIndex = add(TrekTable.stepIndex)
    val stepCount = add(TrekTable.stepCount)
    val intentLabel = add(IntentTable.label)
    val expectedMinutes = add(IntentTable.expectedMins)
    val availableAt = add(TrekTable.availableAt)
    val startedAt = add(TrekTable.startedAt)
}

internal fun ResultRow.toTrekItem() = TrekItem(
    trekId = this[TrekItemAspect.trekId].value,
    stepLabel = this[TrekItemAspect.stepLabel],
    stepIndex = this[TrekItemAspect.stepIndex],
    stepCount = this[TrekItemAspect.stepCount],
    intentLabel = this[TrekItemAspect.intentLabel],
    expectedMinutes = this[TrekItemAspect.expectedMinutes],
    availableAt = this[TrekItemAspect.availableAt].toInstantUtc(),
    startedAt = this[TrekItemAspect.startedAt]?.toInstantUtc()
)

// @Serializable
//data class TrekItem(
//    val trekId: Long,
//    val expectedMinutes: Int,
//    val stepLabel: String,
//    val stepIndex: Int,
//    val stepCount: Int,
//    val intentLabel: String,
//    val availableAt: Instant,
//    val startedAt: Instant,
//)