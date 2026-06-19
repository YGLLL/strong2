package com.ygl.strong.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ygl.strong.http.dto.ReplyDto

/**
 * 评论区面板。
 * 通过 [ReplyViewModel] 加载数据，[aid] 切换时自动重新拉取。
 */
@Composable
fun ReplyPanel(
    aid: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    viewModel: ReplyViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val replies by viewModel.replies.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // aid 变化时触发 ViewModel 加载
    LaunchedEffect(aid, visible) {
        if (visible && aid.isNotEmpty()) {
            viewModel.loadReplies(aid)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
        ) {
            // ── 顶部拖拽指示条 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF666666))
                )
            }

            // ── 标题栏 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "评论",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "关闭",
                    color = Color(0xFF999999),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            // ── 评论列表 / 加载 / 空状态 ──
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("加载中…", color = Color(0xFF999999), fontSize = 14.sp)
                    }
                }
                replies != null && replies!!.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无评论", color = Color(0xFF999999), fontSize = 14.sp)
                    }
                }
                replies != null -> {
                    val list = replies!! // 提前固定非空引用，避免延迟读取
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(list) { reply ->
                            ReplyItem(reply = reply)
                        }
                    }
                }
                // else: 初始状态（replies == null && !isLoading）— 不显示内容，等 LaunchedEffect 触发
            }
        }
    }
}

@Composable
private fun ReplyItem(reply: ReplyDto.RepliesClass) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = reply.member?.uname ?: "匿名",
            color = Color(0xFF999999),
            fontSize = 12.sp
        )
        Text(
            text = reply.content?.message ?: "",
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    // 分割线
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color(0xFF333333))
    )
}
