package com.app.knotes.utils

import androidx.compose.runtime.Composable

@Composable
expect fun KBackHandler(enabled: Boolean = true, onBack: () -> Unit)
