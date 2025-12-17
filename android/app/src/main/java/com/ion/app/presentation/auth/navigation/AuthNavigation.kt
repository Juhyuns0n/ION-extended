package com.ion.app.presentation.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.ion.app.core.navigation.Route
import com.ion.app.presentation.auth.signin.SignInScreenRoute
import com.ion.app.presentation.auth.signup.OnboardingDoneScreenRoute
import com.ion.app.presentation.auth.signup.ParentTypeResultScreenRoute
import com.ion.app.presentation.auth.signup.ParentTypeScreenRoute
import com.ion.app.presentation.auth.signup.UserInfoScreenRoute
import kotlinx.serialization.Serializable

fun NavController.navigateToUserInfo(navOptions: NavOptions? = null) {
    navigate(UserInfo, navOptions)
}
fun NavController.navigateToParentType(navOptions: NavOptions? = null) {
    navigate(ParentType, navOptions)
}

fun NavController.navigateToParentTypeResult(navOptions: NavOptions? = null) {
    navigate(ParentTypeResult, navOptions)
}

fun NavController.navigateToOnboardingDone(navOptions: NavOptions? = null) {
    navigate(OnboardingDone, navOptions)
}

fun NavController.navigateToSignIn(navOptions: NavOptions? = null) {
    navigate(SignIn, navOptions)
}

fun NavGraphBuilder.authGraph(
    navigateToNext: () -> Unit,    // “다음” 눌렀을 때 (온보딩 다음 단계)
    navigateToSignIn: () -> Unit,  // “계정이 이미 있으신가요?” 눌렀을 때
    navigateToUserInfo: () -> Unit,
    navigateToParentTypeResult: () -> Unit,
    navigateToOnboardingDone: () -> Unit,
    navigateToHome: () -> Unit
) {
    composable<UserInfo> {
        UserInfoScreenRoute(
            navigateToNext = navigateToNext,
            navigateToSignIn = navigateToSignIn
        )
    }
    composable<ParentType> {
        ParentTypeScreenRoute(
            navigateToSignIn = navigateToSignIn,
            navigateToParentTypeResult = navigateToParentTypeResult
        )
    }
    composable<ParentTypeResult> {
        ParentTypeResultScreenRoute(
            navigateToOnboardingDone = navigateToOnboardingDone
        )
    }
    composable<OnboardingDone> {
        OnboardingDoneScreenRoute(
            navigateToSignIn = navigateToSignIn
        )
    }
    composable<SignIn> {
        SignInScreenRoute(
            navigateToHome = navigateToHome,
            navigateToSignUp = navigateToUserInfo
        )
    }
}
@Serializable
data object UserInfo : Route

@Serializable
data object ParentType : Route

@Serializable
data object ParentTypeResult : Route

@Serializable
data object OnboardingDone : Route

@Serializable
data object SignIn : Route