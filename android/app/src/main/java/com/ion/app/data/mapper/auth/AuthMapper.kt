package com.ion.app.data.mapper.auth

import com.ion.app.data.dto.request.auth.LoginRequestDto
import com.ion.app.data.dto.response.auth.LoginErrorResponseDto
import com.ion.app.domain.model.auth.LoginError
import com.ion.app.domain.model.auth.LoginParam

fun LoginParam.toDto(): LoginRequestDto = LoginRequestDto(
    email = email,
    password = password
)
fun LoginErrorResponseDto.toDomain(): LoginError = LoginError(
    status = status,
    code = code,
    message = message ?: "로그인에 실패했습니다. 잠시 후 다시 시도해주세요."
)