package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ponder.steps.model.data.StepOutcome

@Entity(
    foreignKeys = [
        ForeignKey(StepEntity::class, ["id"], ["stepId"], ForeignKey.CASCADE),
        ForeignKey(TrekEntity::class, ["id"], ["trekId"], ForeignKey.CASCADE),
    ],
    indices = [
        Index("stepId"),
        Index("trekId")
    ]
)
data class StepLogEntity(
    @PrimaryKey
    val id: String,
    val stepId: String,
    val trekId: String,
    val outcome: StepOutcome,
    val createdAt: Instant,
)