package ponder.steps.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pondui.ui.controls.Row

private val pathMapItemRowModifier = Modifier.height(IntrinsicSize.Max)
private val pathMapLineColumnModifier = Modifier.fillMaxHeight()

@Composable
fun PathMapItemPart(
    lineSlot: @Composable () -> Unit,
    spacingUnits: Int = 1,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        gap = spacingUnits,
        modifier = pathMapItemRowModifier,
        verticalAlignment = verticalAlignment,
    ) {
        Column(
            modifier = pathMapLineColumnModifier
        ) {
            lineSlot()
        }
        content()
    }
}