package com.ygl.strong.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ygl.strong.http.Api
import com.ygl.strong.http.dto.ReplyDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReplyViewModel : ViewModel() {

    private val _replies = MutableStateFlow<List<ReplyDto.RepliesClass>?>(null)
    val replies: StateFlow<List<ReplyDto.RepliesClass>?> = _replies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var loadedAid = ""

    fun loadReplies(aid: String) {
        if (aid.isEmpty() || aid == loadedAid) return
        loadedAid = aid
        _replies.value = null
        _isLoading.value = true

        viewModelScope.launch {
            val body = withContext(Dispatchers.IO) {
                try {
                    Api.BILIBILI.getReplys(pn = "1", oid = aid).execute().body()
                } catch (_: Exception) {
                    null
                }
            }
            val data = body?.data
            _replies.value = if (body != null && body.code == "0" && data != null
                && !data.replies.isNullOrEmpty()
            ) {
                data.replies
            } else {
                emptyList()
            }
            _isLoading.value = false
        }
    }
}
