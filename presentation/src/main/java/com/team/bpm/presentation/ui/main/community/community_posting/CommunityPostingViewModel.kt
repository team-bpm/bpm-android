package com.team.bpm.presentation.ui.main.community.community_posting

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.bpm.domain.usecase.community.WriteCommunityUseCase
import com.team.bpm.presentation.di.IoDispatcher
import com.team.bpm.presentation.di.MainImmediateDispatcher
import com.team.bpm.presentation.util.convertImageBitmapToByteArray
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import java.util.LinkedList
import javax.inject.Inject

@HiltViewModel
class CommunityPostingViewModel @Inject constructor(
    @MainImmediateDispatcher private val mainImmediateDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val writeCommunityUseCase: WriteCommunityUseCase
) : ViewModel(), CommunityPostingContract {
    private val _state = MutableStateFlow(CommunityPostingContract.State())
    override val state: StateFlow<CommunityPostingContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CommunityPostingContract.Effect>()
    override val effect: SharedFlow<CommunityPostingContract.Effect> = _effect.asSharedFlow()

    override fun event(event: CommunityPostingContract.Event) = when (event) {
        is CommunityPostingContract.Event.OnClickImagePlaceHolder -> {
            onClickImagePlaceHolder()
        }

        is CommunityPostingContract.Event.OnClickRemoveImage -> {
            onClickRemoveImage(event.index)
        }

        is CommunityPostingContract.Event.OnImagesAdded -> {
            onImagesAdded(event.images)
        }

        is CommunityPostingContract.Event.OnClickSubmit -> {
            onClickSubmit(event.content)
        }
    }

    private val exceptionHandler: CoroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { coroutineContext, throwable ->

        }
    }

    private fun onClickImagePlaceHolder() {
        viewModelScope.launch {
            _effect.emit(CommunityPostingContract.Effect.AddImages)
        }
    }

    private fun onClickRemoveImage(index: Int) {
        viewModelScope.launch {
            _state.update {
                it.copy(imageList = it.imageList.toMutableList().apply { removeAt(index) })
            }
        }
    }

    private fun onImagesAdded(images: List<Pair<Uri, ImageBitmap>>) {
        val linkedList = LinkedList<Pair<Uri, ImageBitmap>>().apply {
            addAll(state.value.imageList)
        }

        for (i in images.indices) {
            if (linkedList.size == 5) {
                break
            }

            linkedList.addFirst(images[i])
        }

        viewModelScope.launch {
            _state.update {
                it.copy(imageList = linkedList.toMutableList())
            }
        }
    }

    private fun onClickSubmit(content: String) {
        viewModelScope.launch {
            if (content.isNotEmpty()) {
                _state.update {
                    it.copy(isLoading = true)
                }

                withContext(ioDispatcher) {
                    writeCommunityUseCase(content, state.value.imageList.map { image -> convertImageBitmapToByteArray(image.second) }).onEach { result ->
                        withContext(mainImmediateDispatcher) {
                            result.id?.let { communityId -> _effect.emit(CommunityPostingContract.Effect.RedirectToCommunity(communityId)) }
                        }
                    }.launchIn(viewModelScope + exceptionHandler)
                }
            } else {
                _effect.emit(CommunityPostingContract.Effect.ShowToast("내용을 입력해주세요."))
            }
        }
    }
}