package com.team.bpm.presentation.ui.main.mypage.noonbody_posting

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import com.team.bpm.presentation.base.UnidirectionalViewModel

interface NoonBodyPostingContract : UnidirectionalViewModel<NoonBodyPostingContract.State, NoonBodyPostingContract.Event, NoonBodyPostingContract.Effect> {
    data class State(
        val isLoading: Boolean = false,
        val imageList: List<Pair<Uri, ImageBitmap>> = emptyList()
    )

    sealed class Event {
        object OnClickBackButton : Event()
        object OnClickImagePlaceHolder : Event()
        data class OnImagesAdded(val images: List<Pair<Uri, ImageBitmap>>) : Event()
        data class OnClickRemoveImage(val index: Int) : Event()
    }

    sealed class Effect {
        object GoBack : Effect()
        object AddImages : Effect()
        object RemoveImage : Effect()
    }
}