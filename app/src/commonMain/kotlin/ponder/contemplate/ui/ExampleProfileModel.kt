package ponder.contemplate.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.contemplate.ExampleProfileRoute
import ponder.contemplate.io.ExampleStore
import ponder.contemplate.model.data.Example
import pondui.ui.core.StateModel

class ExampleProfileModel(
    route: ExampleProfileRoute,
    private val store: ExampleStore = ExampleStore()
): StateModel<ExampleProfileState>(ExampleProfileState()) {
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