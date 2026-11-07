package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State and gesture controllers for smooth Drag-and-Drop reordering of audio lanes
 */
class LaneDragDropState(
    private val scope: CoroutineScope,
    private val onSwap: (fromIndex: Int, toIndex: Int) -> Unit,
    private val onPersist: () -> Unit
) {
    var draggedLaneId by mutableStateOf<Long?>(null)
        private set

    var dragOffsetY by mutableFloatStateOf(0f)
        private set

    var itemHeightPx by mutableFloatStateOf(300f)

    fun onDragStart(laneId: Long) {
        draggedLaneId = laneId
        dragOffsetY = 0f
    }

    fun onDrag(dragAmountY: Float, currentIndex: Int, totalCount: Int) {
        if (draggedLaneId == null) return
        dragOffsetY += dragAmountY

        val threshold = if (itemHeightPx > 50f) itemHeightPx * 0.45f else 150f

        if (dragOffsetY > threshold && currentIndex < totalCount - 1) {
            // Dragged down past lower neighbor
            onSwap(currentIndex, currentIndex + 1)
            dragOffsetY -= itemHeightPx
        } else if (dragOffsetY < -threshold && currentIndex > 0) {
            // Dragged up past upper neighbor
            onSwap(currentIndex, currentIndex - 1)
            dragOffsetY += itemHeightPx
        }
    }

    fun onDragEnd() {
        if (draggedLaneId != null) {
            draggedLaneId = null
            dragOffsetY = 0f
            onPersist()
        }
    }

    fun onDragCancel() {
        if (draggedLaneId != null) {
            draggedLaneId = null
            dragOffsetY = 0f
            onPersist()
        }
    }
}

@Composable
fun rememberLaneDragDropState(
    onSwap: (fromIndex: Int, toIndex: Int) -> Unit,
    onPersist: () -> Unit
): LaneDragDropState {
    val scope = rememberCoroutineScope()
    return remember(onSwap, onPersist) {
        LaneDragDropState(
            scope = scope,
            onSwap = onSwap,
            onPersist = onPersist
        )
    }
}
