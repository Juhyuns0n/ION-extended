package com.ion.app.presentation.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ion.app.core.util.buildImageUrl
import com.ion.app.domain.repository.home.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageState())
    val uiState: StateFlow<MyPageState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<MyPageSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        // 앱 시작 시 홈 API 한 번 불러와서 드로어에도 같이 씀
        fetchUserInfo()
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            homeRepository.loadHome()
                .onSuccess { home ->
                    val imagePath = buildImageUrl(
                        home.userImage
                    )
                    _uiState.update {
                        it.copy(
                            userInfo = UserInfo(
                                level = home.level,
                                name = home.parentNickname,
                                points = home.points,
                                totalPoints = 50000,
                                userImagePath = home.userImage
                            ),
                            menuSections = buildMenuSections()
                        )
                    }
                }
                .onFailure { e ->
                    e.printStackTrace()
                    // 실패해도 메뉴는 보여야 하니까 메뉴는 세팅해두자
                    _uiState.update {
                        it.copy(
                            userInfo = UserInfo(
                                level = 0,
                                name = "부모님",
                                points = 0,
                                totalPoints = 50000
                            ),
                            menuSections = buildMenuSections()
                        )
                    }
                }
        }
    }

    // --- 사이드바용 메뉴 섹션 정의 ---
    private fun buildMenuSections(): List<MenuSection> = listOf(
        MenuSection(
            title = "내 정보관리",
            items = listOf(
                MenuItem("개인정보 수정", "userInfo")
            )
        ),
        MenuSection(
            title = "고객지원",
            items = listOf(
                MenuItem("자주 묻는 질문(FAQ)", "faq"),
                MenuItem("1:1 문의하기", "inquiry"),
                MenuItem("피드백 보내기", "feedback")
            )
        ),
        MenuSection(
            title = "정책 및 이용정보",
            items = listOf(
                MenuItem("이용약관", "terms"),
                MenuItem("개인정보처리방침", "privacy"),
                MenuItem("오픈소스 라이선스", "license")
            )
        ),
        MenuSection(
            title = "로그아웃 및 회원탈퇴",
            items = listOf(
                MenuItem("로그아웃", "logout"),
                MenuItem("회원탈퇴", "delete")
            ),
        )
    )
}