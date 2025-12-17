package com.ion.app.presentation.auth.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ion.app.domain.model.auth.MembershipRequestModel
import com.ion.app.domain.model.auth.PropensityTestItemModel
import com.ion.app.domain.repository.auth.SignUpRepository
import com.ion.app.domain.repository.workbook.WorkbookRepository
import com.ion.app.presentation.auth.signup.ParentTypeResultUiState
import com.ion.app.presentation.auth.signup.PropensityQuestion
import com.ion.app.presentation.auth.signup.SignUpUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: SignUpRepository,
    private val workbookRepository: WorkbookRepository
) : ViewModel() {

    private var workbookRequested: Boolean = false

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    private val passwordRegex =
        Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#\$%^&*()_+\\-=]{8,20}\$")

    fun onEmailChange(text: String) {
        _uiState.update {
            it.copy(
                email = text,
                step1Error = null
            )
        }
    }

    fun onPasswordChange(text: String) {
        _uiState.update {
            it.copy(
                password = text,
                passwordError = null,
                passwordCheckError = null,
                step1Error = null
            )
        }
    }

    fun onPasswordCheckChange(text: String) {
        _uiState.update {
            it.copy(
                passwordCheck = text,
                passwordError = null,
                passwordCheckError = null,
                step1Error = null
            )
        }
    }

    fun onParentNameChange(text: String) {
        _uiState.update {
            it.copy(
                parentName = text,
                step1Error = null
            )
        }
    }

    fun onParentNicknameChange(text: String) {
        _uiState.update {
            it.copy(
                parentNickname = text,
                step1Error = null
            )
        }
    }

    fun onKidsNicknameChange(value: String) =
        _uiState.update { it.copy(kidsNickname = value) }

    fun onKidsAgeChange(value: String) =
        _uiState.update { it.copy(kidsAge = value) }

    fun onKidsTendencyChange(value: String) =
        _uiState.update { it.copy(kidsTendency = value) }

    fun onKidsNoteChange(value: String) =
        _uiState.update { it.copy(kidsNote = value) }

    fun onGoalChange(value: String) =
        _uiState.update { it.copy(goal = value) }

    fun onWorryChange(value: String) =
        _uiState.update { it.copy(worry = value) }

    fun onPersonalInformationAgreeChange(value: Boolean) =
        _uiState.update {
            it.copy(
                personalInformationAgree = value,
                termsError = null
            )
        }

    fun onNextClicked(onFinished: () -> Unit) {
        val current = _uiState.value

        when (current.step) {
            1 -> {
                if (!validateStep1()) return
                _uiState.update { it.copy(step = 2) }
            }

            2 -> {
                if (!validateStep2()) return
                _uiState.update { it.copy(step = 3) }
            }

            3 -> {
                if (!validateStep3()) return
                if (!current.personalInformationAgree) {
                    _uiState.update {
                        it.copy(
                            termsError = "개인정보 수집·이용에 동의해 주세요."
                        )
                    }
                    return
                }
                _uiState.update { it.copy(step = 4) }
            }

            4 -> _uiState.update { it.copy(step = 5) }

            5 -> onFinished()
        }
    }

    private fun validateStep1(): Boolean {
        val s = _uiState.value

        var passwordError: String? = null
        var passwordCheckError: String? = null
        var stepError: String? = null
        var toast: String? = null
        var ok = true

        // 1) 필수 필드
        if (s.email.isBlank() ||
            s.password.isBlank() ||
            s.passwordCheck.isBlank() ||
            s.parentName.isBlank() ||
            s.parentNickname.isBlank()
        ) {
            stepError = "모든 항목을 입력해주세요."
            toast = stepError
            ok = false
        }

        // 2) 비밀번호 형식
        if (!passwordRegex.matches(s.password)) {
            passwordError = "8~20자, 영문과 숫자를 함께 사용해주세요."
            ok = false
        }

        // 3) 비밀번호 일치
        if (s.password != s.passwordCheck) {
            passwordCheckError = "비밀번호가 일치하지 않습니다."
            ok = false
        }

        _uiState.update {
            it.copy(
                passwordError = passwordError,
                passwordCheckError = passwordCheckError,
                step1Error = stepError,
                toastMessage = toast      // 필수값 빠졌을 때만 토스트
            )
        }

        return ok
    }

    private fun validateStep2(): Boolean {
        val s = _uiState.value

        val missing = s.kidsNickname.isBlank() ||
                s.kidsAge.isBlank() ||
                s.kidsTendency.isBlank() ||
                s.kidsNote.isBlank()

        return if (missing) {
            _uiState.update {
                it.copy(
                    toastMessage = "모든 항목을 입력해주세요."
                )
            }
            false
        } else {
            _uiState.update { it.copy(toastMessage = null) }
            true
        }
    }

    private fun validateStep3(): Boolean {
        val s = _uiState.value

        if (s.goal.isBlank() || s.worry.isBlank()) {
            _uiState.update {
                it.copy(
                    toastMessage = "모든 항목을 입력해주세요."
                )
            }
            return false
        }

        _uiState.update {
            it.copy(
                toastMessage = null,
                termsError = null
            )
        }
        return true
    }

    fun clearTermsError() {
        _uiState.update {
            it.copy(termsError = null)
        }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun onParentTestResultClick(onSuccess: () -> Unit) {
        submitSignUp(onSuccess)
    }

    private fun submitSignUp(onFinished: () -> Unit) {
        Log.d("SignUpViewModel", "submitSignUp() called")
        val current = _uiState.value

        val request = MembershipRequestModel(
            email = current.email,
            password = current.password,
            parentName = current.parentName,
            parentNickname = current.parentNickname,
            kidsNickname = current.kidsNickname,
            kidsAge = current.kidsAge.toIntOrNull() ?: 0,
            kidsTendency = current.kidsTendency,
            kidsNote = current.kidsNote,
            goal = current.goal,
            worry = current.worry,
            personalInformationAgree = if (current.personalInformationAgree) 1 else 0,
            propensityTest = current.propensityQuestions.map {
                PropensityTestItemModel(
                    propensityTestId = it.id,
                    propensityTestQuestion = it.question,
                    propensityTestScore = it.selectedScore
                )
            }
        )

        viewModelScope.launch {
            repository.signUp(request, userImage = null)
                .onSuccess { response ->
                    Log.d("SignUpViewModel", "Sign up success")

                    val userIdLong = response.userId.toLong()

                    _uiState.update { state ->
                        state.copy(userId = response.userId.toString())
                    }
                    onFinished()

                    if (!workbookRequested) {
                        workbookRequested = true

                        launch {
                            workbookRepository.createWorkbook(userIdLong)
                                .onSuccess {
                                    Log.d("SignUpViewModel", "createWorkbook success userId=$userIdLong")
                                }
                                .onFailure { e ->
                                    Log.e("SignUpViewModel", "createWorkbook failed userId=$userIdLong", e)
                                    workbookRequested = false
                                }
                        }
                    }
                }
                .onFailure { e ->
                    Log.e("SignUpViewModel", "Sign up failed", e)
                }
        }
    }

    fun onPrevClicked() {
        _uiState.update { state ->
            if (state.step > 1) state.copy(step = state.step - 1)
            else state
        }
    }

    fun onPropensityScoreChange(id: Int, score: Int) {
        _uiState.update { state ->
            state.copy(
                propensityQuestions = state.propensityQuestions.map { q ->
                    if (q.id == id) q.copy(selectedScore = score) else q
                }
            )
        }
    }

    fun loadPropensityTests() {
        viewModelScope.launch {
            repository.getPropensityTests()
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            propensityQuestions = list.map { q ->
                                PropensityQuestion(
                                    id = q.id,
                                    question = q.question,
                                    selectedScore = 0
                                )
                            }
                        )
                    }
                }
                .onFailure {
                    Log.e("SignUpViewModel", "Error loading tests", it)
                }
        }
    }

    fun loadParentTypeResult(userId: String) {
        viewModelScope.launch {
            repository.getTestResult(userId)
                .onSuccess { res ->

                    val typeKo = when (res.userType) {
                        "authoritative" -> "보호자형"
                        "authoritarian" -> "감독자형"
                        "permissive"    -> "자유로운형"
                        else -> "-"
                    }

                    val description = res.typeExplain + "입니다."

                    val test = res.testScores.firstOrNull()
                    val mainScores = test?.let {
                        "보호자형 ${it.authoritative.roundToInt()}점 · " +
                                "감독자형 ${it.authoritarian.roundToInt()}점 · " +
                                "자유로운형 ${it.permissive.roundToInt()}점"
                    } ?: ""

                    val subScores = res.subtype.map { st ->
                        val ko = subtypeKoLabel(st.type)
                        "$ko: ${st.score.roundToInt()}점"
                    }

                    _uiState.update {
                        it.copy(
                            parentTypeResult = ParentTypeResultUiState(
                                userType = res.userType,
                                userTypeKo = typeKo,
                                description = description,
                                mainScores = mainScores,
                                subScores = subScores,
                                isLoading = false
                            )
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            parentTypeResult = ParentTypeResultUiState(
                                isLoading = false,
                                errorMessage = e.message
                            )
                        )
                    }
                }
        }
    }

    private fun subtypeKoLabel(type: String): String = when (type) {
        "Democratic Participation" -> "아이의 의견을 존중한다"
        "Indulgent Dimension"      -> "아이를 자유롭게 양육한다"
        "Non-Reasoning & Punitive" -> "아이를 이유없이 꾸중한다"
        "Physical Coercion"        -> "아이를 신체적으로 통제한다"
        "Reasoning & Induction"    -> "아이에게 이유를 설명한다"
        "Verbal Hostility"         -> "아이에게 말로 화를 낸다"
        "Warmth & Support"         -> "아이를 따뜻하게 지지한다"
        else -> type
    }
}
