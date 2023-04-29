package com.team.bpm.presentation.ui.main.mypage.eye_body_posting

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.bpm.presentation.base.BaseComponentActivity
import com.team.bpm.presentation.base.BaseViewModel
import com.team.bpm.presentation.base.use
import com.team.bpm.presentation.compose.BPMTextField
import com.team.bpm.presentation.compose.Header
import com.team.bpm.presentation.compose.ImagePlaceHolder
import com.team.bpm.presentation.compose.RoundedCornerButton
import com.team.bpm.presentation.compose.theme.*
import com.team.bpm.presentation.util.convertUriToBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class EyeBodyPostingActivity : BaseComponentActivity() {
    override val viewModel: BaseViewModel
        get() = TODO("Not yet implemented")

    override fun initUi() {
        initComposeUi {
            EyeBodyPostingActivityContent()
        }
    }

    override fun setupCollect() {

    }
}

@Composable
private fun EyeBodyPostingActivityContent(
    viewModel: EyeBodyPostingViewModel = hiltViewModel()
) {
    val (state, event, effect) = use(viewModel)
    val context = LocalContext.current as BaseComponentActivity

    val addImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5),
        onResult = { uris ->
            runCatching {
                uris.map { uri ->
                    convertUriToBitmap(
                        contentResolver = context.contentResolver,
                        uri = uri
                    )
                }
            }.onSuccess { images ->
                event.invoke(EyeBodyPostingContract.Event.OnImagesAdded(images.mapIndexed { index, image ->
                    Pair(uris[index], image.asImageBitmap())
                }))
            }.onFailure {

            }
        })

    LaunchedEffect(Unit) {
        // TODO : Call Api
    }

    LaunchedEffect(effect) {
        effect.collectLatest { _effect ->
            when (_effect) {
                is EyeBodyPostingContract.Effect.GoBack -> {
                    context.finish()
                }
                is EyeBodyPostingContract.Effect.AddImages -> {
                    addImageLauncher.launch(PickVisualMediaRequest())
                }
                is EyeBodyPostingContract.Effect.RemoveImage -> {

                }
            }
        }
    }


    with(state) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Header(
                    title = "오늘의 눈바디 남기기",
                    onClickBackButton = { event.invoke(EyeBodyPostingContract.Event.OnClickBackButton) }
                )

                LazyRow(
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    if (imageList.size < 5) {
                        item {
                            ImagePlaceHolder(
                                image = null,
                                onClick = { event.invoke(EyeBodyPostingContract.Event.OnClickImagePlaceHolder) }
                            )
                        }
                    }

                    itemsIndexed(imageList, key = { _, pair ->
                        pair.first
                    }) { index, pair ->
                        ImagePlaceHolder(
                            image = pair.second,
                            onClick = {},
                            onClickRemove = { event.invoke(EyeBodyPostingContract.Event.OnClickRemoveImage(index)) }
                        )
                    }
                }

                val bodyTextState = remember { mutableStateOf("") }

                BPMTextField(
                    modifier = Modifier
                        .padding(top = 22.dp)
                        .padding(horizontal = 16.dp),
                    textState = bodyTextState,
                    minHeight = 180.dp,
                    limit = 300,
                    hint = "내용을 입력해주세요",
                    label = "오늘의 내 몸에 대한 이야기를 작성해주세요",
                    singleLine = false
                )
            }

            Column {
                Divider(
                    thickness = 1.dp,
                    color = GrayColor8
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(64.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "공개 커뮤니티에 공유",
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        letterSpacing = -(0.17).sp,
                        color = GrayColor4
                    )

                    Box(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(60.dp))
                            .width(66.dp)
                            .height(28.dp)
                            .background(color = GrayColor10)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "오픈예정",
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            letterSpacing = 0.sp,
                            color = GrayColor5
                        )
                    }
                }

                RoundedCornerButton(
                    modifier = Modifier
                        .padding(
                            horizontal = 16.dp,
                            vertical = 14.dp
                        )
                        .fillMaxWidth()
                        .height(48.dp),
                    text = "저장하기",
                    textColor = Color.Black,
                    buttonColor = MainGreenColor,
                    onClick = { }
                )
            }
        }
    }
}