package com.app.knotes

import androidx.compose.ui.window.ComposeUIViewController
import com.app.knotes.di.App

fun MainViewController() = ComposeUIViewController { App() }