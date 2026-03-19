package com.app.knotes.utils

import androidx.compose.runtime.Composable

@Composable
actual fun KBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for iOS as it doesn't have a systemic back button/gesture that needs intercepting in the same way.
    // Navigation is handled at the NavDisplay level.
}
