package com.nassrallah.space

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val _scroll = MutableStateFlow(0)
    val scroll: StateFlow<Int> = _scroll

    fun updateScroll(value: Float) {
        if (value > 0) {
            _scroll.value += 500
        } else {
            _scroll.value -= 500
        }
    }

}