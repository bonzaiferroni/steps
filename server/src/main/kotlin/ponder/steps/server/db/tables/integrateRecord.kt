package ponder.steps.server.db.tables

import kabinet.model.UserId
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.eq
import klutch.utils.fromStringId
import klutch.utils.less
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.upsert
import ponder.steps.model.data.*

internal fun DeletionTable.integrateDeletion(deletion: Deletion, userId: UserId) {
    DeletionTable.insert {
        it[this.id] = deletion.recordId.fromStringId()
        it[this.userId] = userId.fromStringId()
        it[this.entity] = deletion.entity
        it[this.deletedAt] = deletion.deletedAt.toLocalDateTimeUtc()
    }
    val syncType = SyncType.fromClassName(deletion.entity)
    when (syncType) {
        SyncType.StepRecord -> StepTable.deleteWhere {
            StepTable.id.eq(deletion.recordId) and StepTable.userId.eq(userId)
        }
        SyncType.TrekRecord -> TrekTable.deleteWhere {
            TrekTable.id.eq(deletion.recordId) and TrekTable.userId.eq(userId)
        }
        SyncType.IntentRecord -> IntentTable.deleteWhere {
            IntentTable.id.eq(deletion.recordId) and IntentTable.userId.eq(userId)
        }
        SyncType.StepLogRecord -> StepLogTable.deleteWhere {
            StepLogTable.id.eq(deletion.recordId) and StepLogTable.userId.eq(userId)
        }
        SyncType.PathStepRecord -> PathStepTable.deleteWhere {
            PathStepTable.id.eq(deletion.recordId) and PathStepTable.userId.eq(userId)
        }
        SyncType.QuestionRecord -> QuestionTable.deleteWhere {
            QuestionTable.id.eq(deletion.recordId) and QuestionTable.userId.eq(userId)
        }
        SyncType.AnswerRecord -> AnswerTable.deleteWhere {
            AnswerTable.id.eq(deletion.recordId) and AnswerTable.userId.eq(userId)
        }
        SyncType.TagRecord -> TagTable.deleteWhere {
            TagTable.id.eq(deletion.recordId) and TagTable.userId.eq(userId)
        }
        SyncType.StepTagRecord -> StepTagTable.deleteWhere {
            StepTagTable.id.eq(deletion.recordId) and StepTagTable.userId.eq(userId)
        }
    }
}

internal fun AnswerTable.integrateAnswer(answer: Answer, userId: UserId, lastSyncAt: Instant) = upsert(
    where = { AnswerTable.userId.eq(userId) and AnswerTable.updatedAt.less(answer.updatedAt) },
    body = {
        it[this.id] = answer.id.fromStringId()
        it[this.userId] = userId.fromStringId()
        it[this.stepLogId] = answer.stepLogId.fromStringId()
        it[this.questionId] = answer.questionId.fromStringId()
        it[this.value] = answer.value
        it[this.type] = answer.type
        it[this.updatedAt] = answer.updatedAt.toLocalDateTimeUtc()
        it[this.syncAt] = lastSyncAt.toLocalDateTimeUtc()
    }
)

internal fun IntentTable.integrateIntent(intent: Intent, userId: UserId, lastSyncAt: Instant) = upsert(
    where = { IntentTable.userId.eq(userId) and IntentTable.updatedAt.less(intent.updatedAt) },
    body = {
        it[this.id] = intent.id.fromStringId()
        it[this.userId] = userId.fromStringId()
        it[this.rootId] = intent.rootId.fromStringId()
        it[this.label] = intent.label
        it[this.repeatMins] = intent.repeatMins
        it[this.expectedMins] = intent.expectedMins
        it[this.priority] = intent.priority
        it[this.pathIds] = intent.pathIds
        it[this.completedAt] = intent.completedAt?.toLocalDateTimeUtc()
        it[this.scheduledAt] = intent.scheduledAt?.toLocalDateTimeUtc()
        it[this.updatedAt] = intent.updatedAt.toLocalDateTimeUtc()
        it[this.syncAt] = lastSyncAt.toLocalDateTimeUtc()
    }
)

internal fun PathStepTable.integratePathStep(pathStep: PathStep, userId: UserId, lastSyncAt: Instant) = upsert(
    where = { PathStepTable.userId.eq(userId) and PathStepTable.updatedAt.less(pathStep.updatedAt) },
    body = {
        it[this.id] = pathStep.id.fromStringId()
        it[this.userId] = userId.fromStringId()
        it[this.stepId] = pathStep.stepId.fromStringId()
        it[this.pathId] = pathStep.pathId.fromStringId()
        it[this.position] = pathStep.position
        it[this.updatedAt] = pathStep.updatedAt.toLocalDateTimeUtc()
        it[this.syncAt] = lastSyncAt.toLocalDateTimeUtc()
    }
)

internal fun QuestionTable.integrateQuestion(question: Question, userId: UserId, lastSyncAt: Instant) = upsert(
    where = { QuestionTable.userId.eq(userId) and QuestionTable.updatedAt.less(question.updatedAt) },
    body = {
        it[this.id] = question.id.fromStringId()
        it[this.userId] = userId.fromStringId()
        it[this.stepId] = question.stepId.fromStringId()
        it[this.text] = question.text
        it[this.type] = question.type
        it[this.minValue] = question.minValue
        it[this.maxValue] = question.maxValue
        it[this.audioUrl] = question.audioUrl
        it[this.updatedAt] = question.updatedAt.toLocalDateTimeUtc()
        it[this.syncAt] = lastSyncAt.toLocalDateTimeUtc()
    }
)

internal fun StepTable.integrateStep(step: Step, userId: UserId, lastSyncAt: Instant) = upsert(
    where = { StepTable.userId.eq(userId) and StepTable.updatedAt.less(step.updatedAt) },
    body = {
        it[this.id] = step.id.fromStringId()
        it[this.userId] = userId.fromStringId()
        it[this.label] = step.label
        it[this.description] = step.description
        it[this.theme] = step.theme
        it[this.expectedMins] = step.expectedMins
        it[this.imgUrl] = step.imgUrl
        it[this.thumbUrl] = step.thumbUrl
        it[this.audioLabelUrl] = step.audioLabelUrl
        it[this.audioFullUrl] = step.audioFullUrl
        it[this.isPublic] = step.isPublic
        it[this.pathSize] = step.pathSize
        it[this.updatedAt] = step.updatedAt.toLocalDateTimeUtc()
        it[this.createdAt] = step.createdAt.toLocalDateTimeUtc()
        it[this.syncAt] = lastSyncAt.toLocalDateTimeUtc()
    }
)

internal fun StepLogTable.integrateStepLog(stepLog: StepLog, userId: UserId, lastSyncAt: Instant) = upsert(
    where = { StepLogTable.userId.eq(userId) and StepLogTable.updatedAt.less(stepLog.updatedAt) },
    body = {
        it[this.id] = stepLog.id.fromStringId()
        it[this.userId] = userId.fromStringId()
        it[this.stepId] = stepLog.stepId.fromStringId()
        it[this.trekId] = stepLog.trekId?.fromStringId()
        it[this.pathStepId] = stepLog.pathStepId?.fromStringId()
        it[this.outcome] = stepLog.status
        it[this.createdAt] = stepLog.createdAt.toLocalDateTimeUtc()
        it[this.updatedAt] = stepLog.updatedAt.toLocalDateTimeUtc()
        it[this.syncAt] = lastSyncAt.toLocalDateTimeUtc()
    }
)

internal fun StepTagTable.integrateStepTag(stepTag: StepTag, userId: UserId, lastSyncAt: Instant) = upsert(
    where = { StepTagTable.userId.eq(userId) and StepTagTable.updatedAt.less(stepTag.updatedAt) },
    body = {
        it[this.id] = stepTag.id.fromStringId()
        it[this.userId] = userId.fromStringId()
        it[this.stepId] = stepTag.stepId.fromStringId()
        it[this.tagId] = stepTag.tagId.fromStringId()
        it[this.updatedAt] = stepTag.updatedAt.toLocalDateTimeUtc()
        it[this.syncAt] = lastSyncAt.toLocalDateTimeUtc()
    }
)

internal fun TagTable.integrateTag(tag: Tag, userId: UserId, lastSyncAt: Instant) = upsert(
    where = { TagTable.userId.eq(userId) and TagTable.updatedAt.less(tag.updatedAt) },
    body = {
        it[this.id] = tag.id.fromStringId()
        it[this.userId] = userId.fromStringId()
        it[this.label] = tag.label
        it[this.updatedAt] = tag.updatedAt.toLocalDateTimeUtc()
        it[this.syncAt] = lastSyncAt.toLocalDateTimeUtc()
    }
)

internal fun TrekTable.integrateTrek(trek: Trek, userId: UserId, lastSyncAt: Instant) = upsert(
    where = { TrekTable.userId.eq(userId) and TrekTable.updatedAt.less(trek.updatedAt) },
    body = {
        it[this.id] = trek.id.fromStringId()
        it[this.userId] = userId.fromStringId()
        it[this.rootId] = trek.rootId.fromStringId()
        it[this.intentId] = trek.intentId.fromStringId()
        it[this.isComplete] = trek.isComplete
        it[this.createdAt] = trek.createdAt.toLocalDateTimeUtc()
        it[this.finishedAt] = trek.finishedAt?.toLocalDateTimeUtc()
        it[this.expectedAt] = trek.expectedAt?.toLocalDateTimeUtc()
        it[this.updatedAt] = trek.updatedAt.toLocalDateTimeUtc()
        it[this.syncAt] = lastSyncAt.toLocalDateTimeUtc()
    }
)
