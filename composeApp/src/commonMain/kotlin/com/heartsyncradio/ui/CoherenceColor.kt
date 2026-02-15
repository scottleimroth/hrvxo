package com.heartsyncradio.ui

import androidx.compose.ui.graphics.Color

/**
 * Returns a color representing the coherence quality level.
 * Green for high (60%+), amber for moderate (30-60%), red for low (<30%).
 */
fun coherenceColor(score: Double): Color = when {
    score >= 0.6 -> Color(0xFF4CAF50)  // Green 500
    score >= 0.3 -> Color(0xFFFF9800)  // Orange 500
    else -> Color(0xFFF44336)          // Red 500
}
