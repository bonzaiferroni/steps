package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var answerText by remember { mutableStateOf<String?>(null) }

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

    Column(1, modifier = modifier) {
        Row(1, modifier = Modifier.fillMaxWidth()) {
            Label("$stepLabel:", modifier = Modifier.widthIn(max = 100.dp), maxLines = 1)
            Text(question.text, modifier = Modifier.weight(1f), maxLines = 1)
        }
        Row(1) {
            when (dataType) {
                DataType.String -> StringAnswer(fieldText, ::updateAnswer, ::onSubmit)
                DataType.Integer -> IntegerAnswer(fieldText, ::updateAnswer)
                DataType.Decimal -> IntegerAnswer(fieldText, ::updateAnswer)
                DataType.Boolean -> BooleanAnswer(fieldText, ::updateAnswer, ::onSubmit)
                DataType.TimeStamp -> TODO()
            }
            val isAnswering = fieldText.isNotEmpty()
            val hasAnswer = answerText?.isNotEmpty() == true
            Button(
                text = if (isAnswering) "Done" else "Skip",
                background = if (isAnswering) Pond.colors.primary else Pond.colors.secondary,
                isEnabled = !isAnswering || hasAnswer,
                onClick = ::onSubmit,
            )
        }
    }
}

@Composable
fun RowScope.StringAnswer(
    answerText: String,
    changeAnswer: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    TextField(
        text = answerText,
        onTextChange = changeAnswer,
        modifier = Modifier.weight(1f)
            .onEnterPressed(onSubmit)
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
fun RowScope.BooleanAnswer(
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