package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.ExampleRepository
import ponder.steps.model.data.Example
import ponder.steps.model.data.NewExample
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState

class ExampleListModel(
    private val store: ExampleRepository = ExampleRepository()
): StateModel<ExampleListState>() {
    override val state = ModelState(ExampleListState())
    init {
        refreshItems()
    }

    fun refreshItems() {
        viewModelScope.launch {
            val examples = store.readUserExamples()
            setState { it.copy(examples = examples) }
        }
    }

    fun createNewItem() {
        if (!stateNow.isValidNewItem) return
        viewModelScope.launch {
            val exampleId = store.createExample(NewExample(
                label = stateNow.newLabel
            ))
            if (exampleId != null) {
                refreshItems()
                setState { it.copy(newLabel = "", isCreatingItem = false) }
            }
        }
    }

    fun deleteItem(example: Example) {
        viewModelScope.launch {
            val isSuccess = store.deleteExample(example.id)
            if (isSuccess) {
                refreshItems()
            }
        }
    }

    fun setLabel(label: String) {
        setState { it.copy(newLabel = label) }
    }

    fun toggleIsCreatingItem() {
        setState { it.copy(isCreatingItem = !it.isCreatingItem) }
    }
}

data class ExampleListState(
    val examples: List<Example> = emptyList(),
    val newLabel: String = "",
    val isCreatingItem: Boolean = false,
) {
    val isValidNewItem get() = newLabel.isNotBlank()
}
