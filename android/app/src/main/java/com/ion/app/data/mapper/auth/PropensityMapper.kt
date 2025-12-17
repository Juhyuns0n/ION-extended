package com.ion.app.data.mapper.auth

import com.ion.app.data.dto.response.auth.PropensityTestResponseDto
import com.ion.app.domain.model.auth.PropensityQuestionModel

fun PropensityTestResponseDto.toDomain() =
    PropensityQuestionModel(
        id = propensityTestId,
        question = propensityTestQuestion
    )

fun List<PropensityTestResponseDto>.toDomainList() =
    map { it.toDomain() }