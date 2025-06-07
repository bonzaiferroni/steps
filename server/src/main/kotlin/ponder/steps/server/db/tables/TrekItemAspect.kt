package ponder.steps.server.db.tables

import kabinet.utils.toInstantUtc
import klutch.db.Aspect
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import ponder.steps.model.data.TrekItem

internal object TrekItemAspect: Aspect<TrekItemAspect, TrekItem>(
    TrekTable.join(StepTable, JoinType.LEFT, TrekTable.stepId, StepTable.id)
        .join(IntentTable, JoinType.LEFT, TrekTable.intentId, IntentTable.id),
    ResultRow::toTrekItem
) {
    val trekId = add(TrekTable.id)
    val stepLabel = add(StepTable.label)
    val stepPathSize = add(StepTable.pathSize)
    val stepIndex = add(TrekTable.stepIndex)
    val stepCount = add(TrekTable.stepCount)
    val stepImgUrl = add(StepTable.imgUrl)
    val stepThumbUrl = add(StepTable.thumbUrl)
    val intentLabel = add(IntentTable.label)
    val expectedMinutes = add(IntentTable.expectedMins)
    val availableAt = add(TrekTable.availableAt)
    val startedAt = add(TrekTable.startedAt)
    val finishedAt = add(TrekTable.finishedAt)
}

internal fun ResultRow.toTrekItem() = TrekItem(
    trekId = this[TrekItemAspect.trekId].value.toString(),
    stepLabel = this[TrekItemAspect.stepLabel],
    stepPathSize = this[TrekItemAspect.stepPathSize],
    stepIndex = this[TrekItemAspect.stepIndex],
    stepCount = this[TrekItemAspect.stepCount],
    stepImgUrl = this[TrekItemAspect.stepImgUrl],
    stepThumbUrl = this[TrekItemAspect.stepThumbUrl],
    intentLabel = this[TrekItemAspect.intentLabel],
    expectedMinutes = this[TrekItemAspect.expectedMinutes],
    availableAt = this[TrekItemAspect.availableAt].toInstantUtc(),
    startedAt = this[TrekItemAspect.startedAt]?.toInstantUtc(),
    finishedAt = this[TrekItemAspect.finishedAt]?.toInstantUtc()
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