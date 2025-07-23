package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.ExampleProfileRoute
import ponder.steps.io.ExampleRepository
import ponder.steps.model.data.Example
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class ExampleProfileModel(
    route: ExampleProfileRoute,
    private val store: ExampleRepository = ExampleRepository()
): StateModel<ExampleProfileState>() {
    override val state = ViewState(ExampleProfileState())
    init {
        viewModelScope.launch {
            val example = store.readExample(route.exampleId)
            setState { it.copy(example = example, label = example.label) }
        }
    }

    fun toggleEdit() {
        setState { it.copy(isEditing = !it.isEditing) }
    }

    fun setLabel(value: String) {
        setState { it.copy(label = value) }
    }

    fun finalizeEdit() {
        val example = stateNow.example?.copy(label = stateNow.label) ?: return
        viewModelScope.launch {
            val isSuccess = store.updateExample(example)
            if (isSuccess) {
                setState { it.copy(example = example) }
                toggleEdit()
            }
        }
    }
}

data class ExampleProfileState(
    val example: Example? = null,
    val label: String = "",
    val isEditing: Boolean = false
)
