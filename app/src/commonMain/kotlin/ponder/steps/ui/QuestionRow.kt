package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ponder.steps.model.data.Answer
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question
import pondui.ui.behavior.magic
import pondui.ui.behavior.onEnterPressed
import pondui.ui.controls.*
import pondui.ui.controls.ControlSetButton
import pondui.ui.theme.Pond

@Composable
fun QuestionRow(
    question: Question,
    modifier: Modifier = Modifier,
    answerQuestion: (String?) -> Unit
) {
    val dataType = question.type
    var fieldText by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf<String?>(null) }

    fun updateAnswer(value: String) {
        fieldText = value
        answerText = when (dataType) {
            DataType.String -> value.takeIf { it.isNotEmpty() }
            DataType.Integer -> value.toIntOrNull()?.toString()
            DataType.Float -> value.toFloatOrNull()?.toString()
            DataType.Boolean -> value.toBooleanStrictOrNull()?.toString()
        }
    }

    Column(1, modifier = modifier) {
        Text(question.text)
        Row(1) {
            when (dataType) {
                DataType.String -> StringAnswer(fieldText, ::updateAnswer)
                DataType.Integer -> IntegerAnswer(fieldText, ::updateAnswer)
                DataType.Float -> StringAnswer(fieldText, ::updateAnswer)
                DataType.Boolean -> TODO()
            }
            val isAnswering = fieldText.isNotEmpty()
            val hasAnswer = answerText?.isNotEmpty() == true
            Button(
                text = if (isAnswering) "Done" else "Skip",
                background = if (isAnswering) Pond.colors.primary else Pond.colors.secondary,
                isEnabled = !isAnswering || hasAnswer,
                onClick = {
                    fieldText = ""
                    answerQuestion(answerText)
                },
            )
        }
    }
}

@Composable
fun RowScope.StringAnswer(
    answerText: String,
    changeAnswer: (String) -> Unit
) {
    TextField(
        text = answerText,
        onTextChange = changeAnswer,
        modifier = Modifier.weight(1f)
    )
}

@Composable
fun RowScope.IntegerAnswer(
    answerText: String,
    changeAnswer: (String) -> Unit
) {
    val initializedAnswer = answerText.ifEmpty { "0" }
    Box(modifier = Modifier.weight(1f)) {
        ControlSet(modifier = Modifier) {
            AddToNumberButton(-100, initializedAnswer, changeAnswer)
            AddToNumberButton(-10, initializedAnswer, changeAnswer)
            AddToNumberButton(-1, initializedAnswer, changeAnswer)
            TextField(
                text = initializedAnswer,
                onTextChange = changeAnswer,
                modifier = Modifier.width(80.dp),
                maxLines = 1
            )
            AddToNumberButton(1, initializedAnswer, changeAnswer)
            AddToNumberButton(10, initializedAnswer, changeAnswer)
            AddToNumberButton(100, initializedAnswer, changeAnswer)
        }
    }
}

@Composable
fun FlowRowScope.AddToNumberButton(
    quantity: Int,
    answerText: String,
    changeAnswer: (String) -> Unit
) {
    ControlSetButton(
        text = "${if (quantity > 0) "+" else ""}$quantity",
        padding = PaddingValues(horizontal = Pond.ruler.unitSpacing, vertical = Pond.ruler.unitSpacing)
    ) {
        val value = answerText.toIntOrNull() ?: return@ControlSetButton
        changeAnswer((value + quantity).toString())
    }
}