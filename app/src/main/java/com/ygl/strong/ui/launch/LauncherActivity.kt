package com.ygl.strong.ui.launch

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ygl.strong.R
import com.ygl.strong.ui.main.MainActivity
import com.ygl.strong.utils.Constant
import com.ygl.strong.widget.LoadingDialog

class LauncherActivity : ComponentActivity() {

    private var mLoading: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLoading = LoadingDialog(this)
        enableEdgeToEdge()

        setContent {
            val viewModel: LauncherViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // 驱动加载
            LaunchedEffect(Unit) {
                viewModel.loadVideos()
            }

            // 响应状态：LoadingDialog
            LaunchedEffect(uiState) {
                when (uiState) {
                    is LauncherUiState.Loading -> {
                        mLoading?.show(false, "加载中...")
                    }
                    else -> {
                        mLoading?.dismiss()
                    }
                }
            }

            // 响应状态：导航 / Toast
            LaunchedEffect(uiState) {
                when (val state = uiState) {
                    is LauncherUiState.Success -> {
                        startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
                        finish()
                    }
                    is LauncherUiState.Error -> {
                        Toast.makeText(
                            this@LauncherActivity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {}
                }
            }

            LauncherContent(
                buildNumber = Constant.BUILD_NUMBER,
                isDebug = Constant.IS_DEBUG
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLoading = null
    }
}

@Composable
fun LauncherContent(buildNumber: String, isDebug: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                text = stringResource(id = R.string.app_name_upper_case),
                fontSize = 20.sp,
                color = Color(0xFF008000), // green
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 5.dp)
            )

            if (isDebug) {
                Text(
                    text = buildNumber,
                    fontSize = 20.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}
