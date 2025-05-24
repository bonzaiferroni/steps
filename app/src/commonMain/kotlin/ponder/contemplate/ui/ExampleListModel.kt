package ponder.contemplate.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.contemplate.io.ExampleStore
import ponder.contemplate.model.data.Example
import ponder.contemplate.model.data.NewExample
import pondui.ui.core.StateModel

class ExampleListModel(
    private val store: ExampleStore = ExampleStore()
): StateModel<ExampleListState>(ExampleListState()) {
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
                symtrix = stateNow.newSymtrix
            ))
            if (exampleId > 0) {
                refreshItems()
                setState { it.copy(newSymtrix = "") }
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

    fun setSymtrix(symtrix: String) {
        setState { it.copy(newSymtrix = symtrix) }
    }
}

data class ExampleListState(
    val examples: List<Example> = emptyList(),
    val newSymtrix: String = "",
) {
    val isValidNewItem get() = newSymtrix.isNotBlank()
}