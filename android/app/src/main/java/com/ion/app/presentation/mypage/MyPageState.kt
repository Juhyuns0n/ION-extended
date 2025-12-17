package com.ion.app.presentation.mypage

data class MyPageState(
    val userInfo: UserInfo? = null,
    val menuSections: List<MenuSection> = emptyList(),
    val showLogoutModal: Boolean = false,
    val showDeleteAccountModal: Boolean = false
)

data class UserInfo(
    val level: Int = 0,
    val name: String = "_",
    val points: Int = 0,
    val totalPoints: Int = 50000,
    val userImagePath: String? = null,
)

data class MenuItem(
    val title: String,
    val key: String
)

data class MenuSection(
    val title: String,
    val items: List<MenuItem>
)

sealed interface MyPageSideEffect {
    data class OpenUrl(val url: String) : MyPageSideEffect
}

enum class ModalType {
    LOGOUT,
    DELETE_ACCOUNT
}

