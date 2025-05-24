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
            setState { it.copy(example = example, symtrix = example.symtrix) }
        }
    }

    fun toggleEdit() {
        setState { it.copy(isEditing = !it.isEditing) }
    }

    fun setSymtrix(value: String) {
        setState { it.copy(symtrix = value) }
    }

    fun finalizeEdit() {
        val example = stateNow.example?.copy(symtrix = stateNow.symtrix) ?: return
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
    val symtrix: String = "",
    val isEditing: Boolean = false
)