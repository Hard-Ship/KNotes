package com.app.knotes.utils

import androidx.compose.runtime.Composable

@Composable
actual fun KBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for JVM/Desktop
}
