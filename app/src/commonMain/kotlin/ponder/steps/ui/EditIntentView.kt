package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock
import ponder.steps.db.TimeUnit
import ponder.steps.model.data.IntentPriority
import pondui.ui.behavior.Magic
import pondui.ui.behavior.magic
import pondui.ui.controls.Column
import pondui.ui.controls.DateTimeWheel
import pondui.ui.controls.Label
import pondui.ui.controls.MenuWheel
import pondui.ui.controls.Row
import pondui.ui.controls.TimeWheel

@Composable
fun EditIntentView(
    viewModel: EditIntentModel
) {
    val state by viewModel.state.collectAsState()

    Column(1, modifier = Modifier.fillMaxWidth()) {
        Label("Schedule")
        Row(1) {
            MenuWheel(
                selectedItem = state.timing,
                options = IntentTiming.entries.toImmutableList(),
                onSelect = viewModel::setIntentTiming,
            )
            Box {
                Magic(state.timing == IntentTiming.Repeat, offsetX = 40.dp) {
                    Row(1) {
                        Label("every")
                        MenuWheel(
                            selectedItem = state.repeatValue,
                            options = state.repeatValues,
                            onSelect = viewModel::setIntentRepeat,
                        )
                        MenuWheel(
                            selectedItem = state.repeatUnit,
                            options = TimeUnit.entries.toImmutableList(),
                            onSelect = viewModel::setIntentRepeatUnit,
                            itemAlignment = Alignment.Start
                        )
                        val canSChedule = state.repeatUnit > TimeUnit.Hour
                        Row(
                            spacingUnits = 1,
                            modifier = Modifier.magic(canSChedule, offsetX = 40.dp)
                        ) {
                            Label("at")
                            TimeWheel(
                                instant = state.scheduledAt ?: Clock.System.now(),
                                onChangeInstant = viewModel::setScheduleAt,
                            )
                        }
                    }
                }
                Magic(state.timing == IntentTiming.Schedule, offsetX = 40.dp) {
                    Row(1) {
                        Label("at")
                        DateTimeWheel(
                            state.scheduledAt ?: Clock.System.now(),
                            onChangeInstant = viewModel::setScheduleAt
                        )
                    }
                }
            }
        }
        Label("Priority")
        MenuWheel(
            selectedItem = state.priority,
            options = IntentPriority.entries.toImmutableList(),
            onSelect = viewModel::setIntentPriority,
            itemAlignment = Alignment.CenterHorizontally,
        )
    }
}