package com.ion.app.presentation.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.ion.app.core.designsystem.component.backhandler.BackHandler
import com.ion.app.core.designsystem.component.snackbar.CustomSnackBar
import com.ion.app.core.designsystem.event.LocalSnackBarTrigger
import com.ion.app.core.designsystem.ui.theme.IonTheme
import com.ion.app.core.util.screenHeightDp
import com.ion.app.core.util.screenWidthDp
import com.ion.app.presentation.auth.navigation.OnboardingDone
import com.ion.app.presentation.auth.navigation.ParentType
import com.ion.app.presentation.auth.navigation.ParentTypeResult
import com.ion.app.presentation.auth.navigation.SignIn
import com.ion.app.presentation.auth.navigation.UserInfo
import com.ion.app.presentation.home.navigation.Home
import com.ion.app.presentation.main.component.MainBottomBar
import com.ion.app.presentation.main.component.MainTopBar
import com.ion.app.presentation.mypage.MyPageDrawerContent
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navigator: MainNavigator = rememberMainNavigator(),
    viewModel: MainViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var isNavigating by remember { mutableStateOf(false) }

    val currentTab = navigator.currentTab
    val showBottomBar = navigator.showBottomBar()
    val showTopBar = navigator.showTopBar()

    // 현재 destination 가져오기
    val backStackEntry by navigator.navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // 회원가입 플로우인지 여부
    val isAuthFlow = currentDestination?.let { destination ->
        destination.hasRoute(UserInfo::class) ||
                destination.hasRoute(ParentType::class) ||
                destination.hasRoute(ParentTypeResult::class) ||
                destination.hasRoute(OnboardingDone::class) ||
                destination.hasRoute(SignIn::class)
    } == true

    // Snackbar trigger
    val onShowSnackBar: (String) -> Unit = { message ->
        scope.launch {
            snackBarHostState.currentSnackbarData?.dismiss()
            val job = launch {
                snackBarHostState.showSnackbar(message)
            }
            delay(3000L)
            job.cancel()
        }
    }

    // Drawer 열려 있을 때 뒤로가기는 "닫기"만, 회원가입 플로우에선 Drawer 없음
    if (!isAuthFlow && drawerState.isOpen) {
        BackHandler { scope.launch { drawerState.close() } }
    }

    // 루트 탭에서만 두 번 눌러 종료 (회원가입 플로우 제외)
    if (!isAuthFlow && showBottomBar) {
        if (currentTab in listOf(
                MainNavTab.HOME,
                MainNavTab.WORKBOOK,
                MainNavTab.VOICE,
                MainNavTab.CHATBOT
            )
        ) {
            BackHandler("뒤로가기를 한 번 더 누르면 앱이 종료됩니다")
        }
    }

    CompositionLocalProvider(LocalSnackBarTrigger provides onShowSnackBar) {

        // 회원가입 flow(예: UserInfo)에서는 Drawer 없이 바로 Scaffold
        if (isAuthFlow) {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackBarHostState,
                        modifier = Modifier
                            .padding(horizontal = screenWidthDp(24.dp))
                            .padding(bottom = screenHeightDp(68.dp))
                    ) { snackBar ->
                        CustomSnackBar(message = snackBar.visuals.message)
                    }
                },
                // 회원가입 플로우에서는 TopBar / BottomBar 둘 다 숨김
                modifier = Modifier
                    .fillMaxSize()
                    .background(IonTheme.colors.black)
            ) {
                MainNavHost(
                    navigator = navigator,
                    toppadding = it.calculateTopPadding(),
                    bottompadding = it.calculateBottomPadding(),
                    modifier = Modifier
                )
            }
        } else {
            // 일반 메인 플로우: Drawer + TopBar + BottomBar 다 사용
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    MyPageDrawerContent(onItemClick = { /* TODO */ })
                }
            ) {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackBarHostState,
                            modifier = Modifier
                                .padding(horizontal = screenWidthDp(24.dp))
                                .padding(bottom = screenHeightDp(68.dp))
                        ) { snackBar ->
                            CustomSnackBar(message = snackBar.visuals.message)
                        }
                    },
                    topBar = {
                        if (showTopBar)
                            MainTopBar(onMenuClick = { scope.launch { drawerState.open() } })
                    },
                    bottomBar = {
                        MainBottomBar(
                            visible = navigator.showBottomBar(),
                            tabs = MainNavTab.entries.toImmutableList(),
                            currentTab = currentTab,
                            onTabSelected = { selectedTab ->
                                if (isNavigating || selectedTab == currentTab) return@MainBottomBar
                                scope.launch {
                                    isNavigating = true
                                    try {
                                        val navOptions = navOptions {
                                            popUpTo(Home) {
                                                saveState = true
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        navigator.navigate(selectedTab)
                                    } finally {
                                        isNavigating = false
                                    }
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(IonTheme.colors.black)
                ) {
                    MainNavHost(
                        navigator = navigator,
                        toppadding = it.calculateTopPadding(),
                        bottompadding = it.calculateBottomPadding(),
                        modifier = Modifier
                    )
                }
            }
        }
    }
}
