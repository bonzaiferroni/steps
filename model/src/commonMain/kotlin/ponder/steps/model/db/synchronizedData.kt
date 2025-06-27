package ponder.steps.model.db

import ponder.steps.model.data.Answer
import ponder.steps.model.data.Intent
import ponder.steps.model.data.PathStep
import ponder.steps.model.data.Question
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepTag
import ponder.steps.model.data.Tag
import ponder.steps.model.data.Trek

val synchronizedData = listOf(
    Step::class,
    PathStep::class,
    Question::class,
    Intent::class,
    Trek::class,
    StepLog::class,
    Answer::class,
    Tag::class,
    StepTag::class,
)