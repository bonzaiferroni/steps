package ponder.steps.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question
import pondui.ui.behavior.onEnterPressed
import pondui.ui.controls.*
import pondui.ui.controls.ControlSetButton
import pondui.ui.theme.Pond
import pondui.utils.lighten

@Composable
fun QuestionRow(
    stepLabel: String,
    question: Question,
    modifier: Modifier = Modifier,
    answerQuestion: (String?) -> Unit
) {
    val dataType = question.type
    var fieldText by remember { mutableStateOf("") }
    val initialAnswerText = when (dataType) {
        DataType.Integer -> "0"
        else -> null
    }
    var answerText by remember { mutableStateOf(initialAnswerText) }

    fun onSubmit() {
        fieldText = ""
        answerQuestion(answerText)
    }

    fun updateAnswer(value: String) {
        fieldText = value
        answerText = when (dataType) {
            DataType.String -> value.takeIf { it.isNotEmpty() }
            DataType.Integer -> value.toIntOrNull()?.toString()
            DataType.Decimal -> value.toFloatOrNull()?.toString()
            DataType.Boolean -> value.toBooleanStrictOrNull()?.toString()
            DataType.TimeStamp -> value
        }
    }

    Section(modifier = modifier) {
        Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(question.text, maxLines = 1)
            Row(1) {
                when (dataType) {
                    DataType.String -> StringAnswer(fieldText, ::updateAnswer, ::onSubmit)
                    DataType.Integer -> IntegerAnswer(fieldText, ::updateAnswer)
                    DataType.Decimal -> IntegerAnswer(fieldText, ::updateAnswer)
                    DataType.Boolean -> BooleanAnswer(fieldText, ::updateAnswer, ::onSubmit)
                    DataType.TimeStamp -> StringAnswer(fieldText, ::updateAnswer, ::onSubmit)
                }
            }
            Row(1) {
                Label(
                    text = "from $stepLabel",
                    modifier = Modifier.padding(start = Pond.ruler.doubleSpacing).weight(1f)
                )
                val isAnswering = fieldText.isNotEmpty()
                val hasAnswer = answerText?.isNotEmpty() == true
                ControlSet {
                    ControlSetButton(
                        text = "Skip",
                        background = Pond.colors.secondary,
                        onClick = ::onSubmit,
                    )
                    ControlSetButton(
                        text = "Done",
                        background = Pond.colors.primary,
                        isEnabled = !isAnswering || hasAnswer,
                        onClick = ::onSubmit,
                    )
                }
            }
        }
    }
}

@Composable
fun StringAnswer(
    answerText: String,
    changeAnswer: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    TextField(
        text = answerText,
        onTextChanged = changeAnswer,
        modifier = Modifier.onEnterPressed(onSubmit)
    )
}

@Composable
fun IntegerAnswer(
    answerText: String,
    changeAnswer: (String) -> Unit
) {
    val initializedAnswer = answerText.ifEmpty { "0" }
    ControlSet(modifier = Modifier) {
        AddToNumberButton(-100, initializedAnswer, changeAnswer)
        AddToNumberButton(-10, initializedAnswer, changeAnswer)
        AddToNumberButton(-1, initializedAnswer, changeAnswer)
        TextField(
            text = initializedAnswer,
            onTextChanged = changeAnswer,
            modifier = Modifier.width(80.dp),
            maxLines = 1
        )
        AddToNumberButton(1, initializedAnswer, changeAnswer)
        AddToNumberButton(10, initializedAnswer, changeAnswer)
        AddToNumberButton(100, initializedAnswer, changeAnswer)
    }
}

@Composable
fun BooleanAnswer(
    answerText: String,
    changeAnswer: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    ControlSet {
        val selectedAnswer = answerText.toBooleanStrictOrNull()
        val bgColor = Pond.colors.secondary
        val selectedColor = bgColor.lighten()
        val toBg: (Boolean?) -> Color = { value -> if (value == true) selectedColor else bgColor }
        Button("True", toBg(selectedAnswer)) { changeAnswer(true.toString()); onSubmit() }
        Button("False", toBg(selectedAnswer)) { changeAnswer(false.toString()); onSubmit() }
    }
}

@Composable
fun AddToNumberButton(
    quantity: Int,
    answerText: String,
    changeAnswer: (String) -> Unit
) {
    Button(
        text = "${if (quantity > 0) "+" else ""}$quantity",
        padding = PaddingValues(horizontal = Pond.ruler.unitSpacing, vertical = Pond.ruler.unitSpacing)
    ) {
        val value = answerText.toIntOrNull() ?: return@Button
        changeAnswer((value + quantity).toString())
    }
}