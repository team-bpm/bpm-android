package com.team.bpm.presentation.ui.main.bodyshape.detail.posting

import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.team.bpm.domain.usecase.body_shape.EditBodyShapeUseCase
import com.team.bpm.domain.usecase.body_shape.GetBodyShapeUseCase
import com.team.bpm.domain.usecase.body_shape.WriteBodyShapeUseCase
import com.team.bpm.presentation.base.BaseViewModelV2
import com.team.bpm.presentation.util.convertImageBitmapToByteArray
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BodyShapeDetailPostingViewModel @Inject constructor(
    private val getBodyShapeUseCase: GetBodyShapeUseCase,
    private val writeBodyShapeUseCase: WriteBodyShapeUseCase,
    private val editBodyShapeUseCase: EditBodyShapeUseCase,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModelV2(),
    BodyShapeDetailPostingContract {
    private val _state = MutableStateFlow(BodyShapeDetailPostingContract.State())
    override val state: StateFlow<BodyShapeDetailPostingContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<BodyShapeDetailPostingContract.Effect>()
    override val effect: SharedFlow<BodyShapeDetailPostingContract.Effect> = _effect.asSharedFlow()

    override fun event(event: BodyShapeDetailPostingContract.Event) = when (event) {
        is BodyShapeDetailPostingContract.Event.GetBodyShapeContent -> {
            getBodyShapeContent()
        }

        is BodyShapeDetailPostingContract.Event.SetImageListWithLoadedImageList -> {
            setImageListWithLoadedImageList(event.loadedImageList)
        }

        is BodyShapeDetailPostingContract.Event.OnClickImagePlaceHolder -> {
            onClickImagePlaceHolder()
        }

        is BodyShapeDetailPostingContract.Event.OnClickRemoveImage -> {
            onClickRemoveImage(event.index)
        }

        is BodyShapeDetailPostingContract.Event.OnImagesAdded -> {
            onImagesAdded(event.images)
        }

        is BodyShapeDetailPostingContract.Event.OnClickSubmit -> {
            onClickSubmit(event.content)
        }
    }

    private val exceptionHandler: CoroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { coroutineContext, throwable ->

        }
    }

    private fun getBundle(): Bundle? {
        return savedStateHandle.get<Bundle>(BodyShapeDetailPostingActivity.KEY_BUNDLE)
    }

    private val getBodyShapeInfo: Pair<Int?, Int?> by lazy {
        Pair(
            getBundle()?.getInt(BodyShapeDetailPostingActivity.KEY_ALBUM_ID) ?: 33,
            getBundle()?.getInt(BodyShapeDetailPostingActivity.KEY_BODY_SHAPE_ID) ?: 1,
        )
    }

    private fun getBodyShapeContent() {
        getBodyShapeInfo.first?.let { albumId ->
            getBodyShapeInfo.second?.let { bodyShapeId ->
                viewModelScope.launch {
                    _state.update {
                        it.copy(isLoading = true)
                    }

                    withContext(ioDispatcher) {
                        getBodyShapeUseCase(albumId, bodyShapeId).onEach { result ->
                            result.content?.let { content ->
                                result.filesPath?.let { filesPath ->
                                    withContext(mainImmediateDispatcher) {
                                        _state.update {
                                            it.copy(isEditing = true)
                                        }

                                        _effect.emit(
                                            BodyShapeDetailPostingContract.Effect.OnContentLoaded(
                                                content,
                                                filesPath
                                            )
                                        )
                                    }
                                }
                            }
                        }.launchIn(viewModelScope + exceptionHandler)
                    }
                }
            }
        }
    }

    private fun setImageListWithLoadedImageList(loadedImageList: List<Pair<Uri, ImageBitmap>>) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = false,
                    imageList = loadedImageList
                )
            }
        }
    }

    private fun onClickImagePlaceHolder() {
        viewModelScope.launch {
            _effect.emit(BodyShapeDetailPostingContract.Effect.AddImages)
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

        _state.update {
            it.copy(imageList = linkedList.toMutableList())
        }
    }

    private fun onClickSubmit(content: String) {
        getBodyShapeInfo.first?.let { albumId ->
            viewModelScope.launch {
                if (content.isNotEmpty()) {
                    _state.update {
                        it.copy(isLoading = true)
                    }

                    withContext(ioDispatcher) {
                        val imageList = state.value.imageList.map { image -> convertImageBitmapToByteArray(image.second) }

                        if (state.value.isEditing) {
                            getBodyShapeInfo.second?.let { bodyShapeId ->
                                editBodyShapeUseCase(albumId, bodyShapeId, content, imageList)
                            }
                        } else {
                            writeBodyShapeUseCase(albumId, content, imageList)
                        }?.onEach { result ->
                            withContext(mainImmediateDispatcher) {
                                result.id?.let { bodyShapeId ->
                                    _effect.emit(
                                        BodyShapeDetailPostingContract.Effect.RedirectToBodyShape(
                                            albumId,
                                            bodyShapeId
                                        )
                                    )
                                }
                            }
                        }?.launchIn(viewModelScope + exceptionHandler)
                    }
                } else {
                    _effect.emit(BodyShapeDetailPostingContract.Effect.ShowToast("내용을 입력해주세요."))
                }
            }
        }
    }
}