package com.ion.app.data.mapper.auth

import com.ion.app.data.dto.request.auth.MembershipRequestDto
import com.ion.app.data.dto.request.auth.PropensityTestRequestDto
import com.ion.app.data.dto.response.auth.MembershipResponseDto
import com.ion.app.data.dto.response.auth.MembershipResultResponseDto
import com.ion.app.data.dto.response.auth.SubtypeDto
import com.ion.app.data.dto.response.auth.TestScoreDto
import com.ion.app.domain.model.auth.*

fun MembershipResultResponseDto.toDomain(): MembershipResultModel =
    MembershipResultModel(
        listScores = listScores.map { map ->    // String -> Int
            map.mapKeys { (key, _) -> key.toInt() }
        },
        userType = userType,
        typeExplain = typeExplain,
        testScores = testScores.map { it.toDomain() },
        subtype = subtype.map { it.toDomain() }
    )

fun TestScoreDto.toDomain(): TestScoreModel =
    TestScoreModel(
        authoritative = authoritative,
        authoritarian = authoritarian,
        permissive = permissive
    )

fun SubtypeDto.toDomain(): SubtypeModel =
    SubtypeModel(
        type = type,
        score = score
    )

fun MembershipResponseDto.toDomain(): MembershipResponseModel =
    MembershipResponseModel(
        userId = userId
    )

fun MembershipRequestModel.toDto(): MembershipRequestDto =
    MembershipRequestDto(
        email = email,
        password = password,
        parentName = parentName,
        parentNickname = parentNickname,
        kidsNickname = kidsNickname,
        kidsAge = kidsAge,
        kidsTendency = kidsTendency,
        kidsNote = kidsNote,
        goal = goal,
        worry = worry,
        personalInformationAgree = personalInformationAgree,
        propensityTest = propensityTest.map { it.toDto() }
    )

fun PropensityTestItemModel.toDto(): PropensityTestRequestDto =
    PropensityTestRequestDto(
        propensityTestId = propensityTestId,
        propensityTestQuestion = propensityTestQuestion,
        propensityTestScore = propensityTestScore
    )

