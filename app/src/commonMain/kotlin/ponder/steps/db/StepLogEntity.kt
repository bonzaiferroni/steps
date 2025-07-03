package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome

@Entity(
    foreignKeys = [
        ForeignKey(StepEntity::class, ["id"], ["stepId"], ForeignKey.CASCADE),
        ForeignKey(TrekEntity::class, ["id"], ["trekId"], ForeignKey.CASCADE),
        ForeignKey(PathStepEntity::class, ["id"], ["pathStepId"], ForeignKey.CASCADE)
    ],
    indices = [Index("stepId"), Index("trekId"), Index("pathStepId")],
)
data class StepLogEntity(
    @PrimaryKey
    val id: String,
    val trekId: String?,
    val stepId: String,
    val pathStepId: String?,
    val outcome: StepOutcome,
    val updatedAt: Instant,
    val createdAt: Instant,
)

fun StepLogEntity.toStepLog() = StepLog(
    id = id,
    trekId = trekId,
    stepId = stepId,
    pathStepId = pathStepId,
    outcome = outcome,
    updatedAt = updatedAt,
    createdAt = createdAt
)

fun StepLog.toEntity() = StepLogEntity(
    id = id,
    trekId = trekId,
    stepId = stepId,
    pathStepId = pathStepId,
    outcome = outcome,
    updatedAt = updatedAt,
    createdAt = createdAt
)
