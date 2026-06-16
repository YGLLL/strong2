package com.ygl.strong.ui.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ygl.strong.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed interface LauncherUiState {
    data object Idle : LauncherUiState
    data object Loading : LauncherUiState
    data object Success : LauncherUiState
    data class Error(val message: String) : LauncherUiState
}

class LauncherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LauncherUiState>(LauncherUiState.Idle)
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    /**
     * 加载视频数据，仅当状态为 Idle 时触发。
     */
    fun loadVideos() {
        if (_uiState.value !is LauncherUiState.Idle) return

        _uiState.value = LauncherUiState.Loading

        viewModelScope.launch {
            val msg = loadVideoDataSuspend()
            _uiState.value = if (msg.isEmpty()) {
                LauncherUiState.Success
            } else {
                LauncherUiState.Error(msg)
            }
        }
    }

    /**
     * 将回调式网络调用转为挂起函数。
     */
    private suspend fun loadVideoDataSuspend(): String = suspendCoroutine { cont ->
        Utils.loadVideoDataByNetwork { msg ->
            cont.resume(msg)
        }
    }
}
