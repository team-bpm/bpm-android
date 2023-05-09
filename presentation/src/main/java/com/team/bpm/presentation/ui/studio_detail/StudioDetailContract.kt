package com.team.bpm.presentation.ui.studio_detail

import com.team.bpm.domain.model.Review
import com.team.bpm.domain.model.Studio
import com.team.bpm.presentation.base.BaseContract
import com.team.bpm.presentation.model.StudioDetailTabType


interface StudioDetailContract : BaseContract<StudioDetailContract.State, StudioDetailContract.Event, StudioDetailContract.Effect> {
    data class State(
        val isLoading: Boolean = false,
        val studio: Studio? = null,
        val originalReviewList: List<Review>? = null,
        val reviewList: List<Review>? = null,
        val isErrorDialogShowing: Boolean = false,
        val focusedTab: StudioDetailTabType = StudioDetailTabType.Info,
        val isReviewListShowingImageReviewsOnly: Boolean = false,
        val isReviewListSortedByLike: Boolean = true,
        val isTagListExpanded: Boolean = false
    )

    sealed interface Event {
        object GetStudioDetailData : Event
        object OnErrorOccurred : Event
        object OnClickQuit : Event
        object OnClickInfoTab : Event
        object OnClickReviewTab : Event
        object OnScrolledAtInfoArea : Event
        object OnScrolledAtReviewArea : Event
        data class OnClickCall(val number: String) : Event
        data class OnClickCopyAddress(val address: String) : Event
        data class OnClickNavigate(val address: String) : Event
        object OnMissingNavigationApp : Event
        object OnClickEditInfoSuggestion : Event
        object OnClickWriteReview : Event
        object OnClickMoreReviews : Event
        object OnClickShowImageReviewsOnly : Event
        object OnClickShowNotOnlyImageReviews : Event
        object OnClickSortByLike : Event
        object OnClickSortByDate : Event
        object OnClickExpandTagList : Event
        object OnClickCollapseTagList : Event
    }

    sealed interface Effect {
        object LoadFailed : Effect
        object Quit : Effect
        object ScrollToInfoTab : Effect
        object ScrollToReviewTab : Effect
        data class Call(val number: String) : Effect
        data class CopyAddressToClipboard(val address: String) : Effect
        data class LaunchNavigationApp(val address: String) : Effect
        data class ShowToast(val text: String) : Effect
        object GoToRegisterStudio : Effect
        object GoToWriteReview : Effect
        object GoToReviewList : Effect
    }
}