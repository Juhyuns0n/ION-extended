package com.ion.app.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


@HiltViewModel
class SplashViewModel @Inject constructor(
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<SplashStateSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()


    //Todo: 로그인, 회원가입 구현
    fun startLogin() {
        viewModelScope.launch {
                _sideEffect.emit(SplashStateSideEffect.NavigateToHome)
        }
    }

    fun startSignUp() {
        viewModelScope.launch {
            _sideEffect.emit(SplashStateSideEffect.NavigateToUserInfo)
        }
    }

}
