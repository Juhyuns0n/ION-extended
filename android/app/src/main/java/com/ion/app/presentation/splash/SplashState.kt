package com.ion.app.presentation.splash

sealed interface SplashStateSideEffect {
    data object NavigateToHome : SplashStateSideEffect
    data object NavigateToUserInfo : SplashStateSideEffect
    data object NavigateToTermsOfService : SplashStateSideEffect
    data class ShowSnackBar(val message: String) : SplashStateSideEffect
}
