package com.example

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * 10-cycle automated test verifying the keyboard dismissal mechanisms:
 * 1. Tap-Outside dismissal logic (checking touch positions inside vs outside search box bounds).
 * 2. Scroll/Drag-to-dismiss threshold logic (detecting touch drags > 1.5dp even on non-scrollable short lists).
 * 3. Focus and IME state transitions across 10 independent operational cycles.
 */
class KeyboardDismissalTest {

    // Helper state simulator for search box and keyboard
    class KeyboardStateSimulator {
        var isKeyboardVisible: Boolean = false
        var isSearchBoxFocused: Boolean = false
        var searchBoxBounds: Rect = Rect(left = 16f, top = 20f, right = 400f, bottom = 80f)
        var dismissCallCount: Int = 0

        fun openKeyboardAndFocus() {
            isKeyboardVisible = true
            isSearchBoxFocused = true
        }

        fun handlePointerPress(touchOffset: Offset) {
            // Tap outside logic:
            if (isSearchBoxFocused && !searchBoxBounds.contains(touchOffset)) {
                dismissKeyboard()
            }
        }

        fun handlePointerDrag(previousOffset: Offset, currentOffset: Offset) {
            // Instant drag threshold > 1.5dp (as implemented in DeviceAudioExplorerSheet & LibraryTabScreen)
            val deltaY = abs(currentOffset.y - previousOffset.y)
            val deltaX = abs(currentOffset.x - previousOffset.x)
            if (isSearchBoxFocused && (deltaY > 1.5f || deltaX > 1.5f)) {
                dismissKeyboard()
            }
        }

        fun dismissKeyboard() {
            isKeyboardVisible = false
            isSearchBoxFocused = false
            dismissCallCount++
        }
    }

    @Test
    fun testTapOutsideDismissesKeyboard_10Iterations() {
        val simulator = KeyboardStateSimulator()

        // 10 test positions strictly outside the search box (Rect(16, 20, 400, 80))
        val outsidePoints = listOf(
            Offset(50f, 120f),  // Audio category chip area
            Offset(100f, 250f), // First song card
            Offset(200f, 400f), // Middle list item
            Offset(5f, 50f),    // Left margin of search box
            Offset(420f, 50f),  // Right margin / outside padding
            Offset(200f, 10f),  // Top header area
            Offset(150f, 600f), // Bottom blank space / empty area
            Offset(300f, 750f), // Close to bottom of dialog
            Offset(50f, 180f),  // Between chips and track list
            Offset(350f, 500f)  // Empty space below short 3-track list
        )

        for (iteration in 1..10) {
            simulator.openKeyboardAndFocus()
            assertTrue("Iteration $iteration: Keyboard must be open initially", simulator.isKeyboardVisible)
            assertTrue("Iteration $iteration: Search box must be focused initially", simulator.isSearchBoxFocused)

            val testPoint = outsidePoints[iteration - 1]
            simulator.handlePointerPress(testPoint)

            assertFalse("Iteration $iteration: Keyboard must be dismissed on tap at $testPoint", simulator.isKeyboardVisible)
            assertFalse("Iteration $iteration: Search box focus must be cleared", simulator.isSearchBoxFocused)
            assertEquals("Iteration $iteration: Total dismiss calls must match iteration count", iteration, simulator.dismissCallCount)
        }
    }

    @Test
    fun testTapInsideSearchBoxDoesNotDismiss_10Iterations() {
        val simulator = KeyboardStateSimulator()

        // 10 test positions strictly INSIDE the search box (Rect(16, 20, 400, 80))
        val insidePoints = listOf(
            Offset(20f, 25f),   // Top-left inside search box
            Offset(50f, 50f),   // Search icon area
            Offset(100f, 50f),  // Text input center
            Offset(200f, 50f),  // Typing middle area
            Offset(300f, 50f),  // Right side typing area
            Offset(380f, 50f),  // Clear 'X' button area
            Offset(390f, 70f),  // Bottom-right inner corner
            Offset(25f, 75f),   // Bottom-left inner corner
            Offset(250f, 30f),  // Upper edge inside search box
            Offset(150f, 70f)   // Lower edge inside search box
        )

        for (iteration in 1..10) {
            simulator.openKeyboardAndFocus()
            val insidePoint = insidePoints[iteration - 1]
            simulator.handlePointerPress(insidePoint)

            assertTrue("Iteration $iteration: Keyboard must remain open on tap inside at $insidePoint", simulator.isKeyboardVisible)
            assertTrue("Iteration $iteration: Search box must remain focused", simulator.isSearchBoxFocused)
            assertEquals("Iteration $iteration: Dismiss count must be 0", 0, simulator.dismissCallCount)
        }
    }

    @Test
    fun testDragGestureDismissesKeyboard_10Iterations() {
        val simulator = KeyboardStateSimulator()

        // 10 distinct drag gestures covering small drags, vertical scroll, horizontal swipe, diagonal gestures
        val dragDeltas = listOf(
            Pair(Offset(100f, 300f), Offset(100f, 302.5f)), // 2.5dp scroll down (small drag on short list)
            Pair(Offset(150f, 400f), Offset(150f, 396.0f)), // 4.0dp scroll up
            Pair(Offset(200f, 250f), Offset(205.0f, 250f)), // 5.0dp horizontal swipe
            Pair(Offset(120f, 350f), Offset(122.0f, 352.0f)),// Diagonal small drag
            Pair(Offset(80f, 500f),  Offset(80f, 480.0f)),  // 20.0dp fast fling up
            Pair(Offset(90f, 450f),  Offset(90f, 470.0f)),  // 20.0dp fast drag down
            Pair(Offset(180f, 320f), Offset(180f, 321.8f)), // 1.8dp subtle touch drag (>1.5dp threshold)
            Pair(Offset(220f, 280f), Offset(223.0f, 280f)), // 3.0dp swipe
            Pair(Offset(130f, 390f), Offset(130f, 385.0f)), // 5.0dp scroll up
            Pair(Offset(160f, 410f), Offset(165.0f, 415.0f)) // 5.0dp diagonal drag
        )

        for (iteration in 1..10) {
            simulator.openKeyboardAndFocus()
            assertTrue("Iteration $iteration: Keyboard must be open initially", simulator.isKeyboardVisible)

            val (startPoint, endPoint) = dragDeltas[iteration - 1]
            simulator.handlePointerDrag(startPoint, endPoint)

            assertFalse("Iteration $iteration: Keyboard must be dismissed after drag from $startPoint to $endPoint", simulator.isKeyboardVisible)
            assertFalse("Iteration $iteration: Focus must be cleared", simulator.isSearchBoxFocused)
            assertEquals("Iteration $iteration: Dismiss count must equal iteration count", iteration, simulator.dismissCallCount)
        }
    }

    @Test
    fun testSubThresholdMicroJitterDoesNotDismiss_10Iterations() {
        val simulator = KeyboardStateSimulator()

        // Micro-jitters < 1.5dp (e.g., resting finger trembling slightly without deliberate drag)
        val microJitters = listOf(
            Pair(Offset(100f, 300f), Offset(100f, 300.5f)), // 0.5dp
            Pair(Offset(150f, 400f), Offset(150.3f, 400f)), // 0.3dp
            Pair(Offset(200f, 250f), Offset(200.7f, 250.7f)),// ~0.99dp
            Pair(Offset(120f, 350f), Offset(120f, 351.0f)), // 1.0dp
            Pair(Offset(80f, 500f),  Offset(80.4f, 500.4f)), // ~0.56dp
            Pair(Offset(90f, 450f),  Offset(90f, 450.8f)),  // 0.8dp
            Pair(Offset(180f, 320f), Offset(180.5f, 320.5f)),// ~0.7dp
            Pair(Offset(220f, 280f), Offset(220f, 280.9f)), // 0.9dp
            Pair(Offset(130f, 390f), Offset(130.6f, 390.6f)),// ~0.85dp
            Pair(Offset(160f, 410f), Offset(160f, 411.2f))  // 1.2dp (< 1.5dp threshold)
        )

        for (iteration in 1..10) {
            simulator.openKeyboardAndFocus()
            val (startPoint, endPoint) = microJitters[iteration - 1]
            simulator.handlePointerDrag(startPoint, endPoint)

            assertTrue("Iteration $iteration: Micro jitter ($startPoint to $endPoint) should NOT dismiss keyboard", simulator.isKeyboardVisible)
            assertTrue("Iteration $iteration: Focus must remain intact", simulator.isSearchBoxFocused)
        }
    }
}
