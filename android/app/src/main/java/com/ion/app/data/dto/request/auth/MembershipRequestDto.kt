package com.ion.app.data.dto.request.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MembershipRequestDto(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,

    @SerialName("parentName")
    val parentName: String,
    @SerialName("parentNickname")
    val parentNickname: String,

    @SerialName("kidsNickname")
    val kidsNickname: String,
    @SerialName("kidsAge")
    val kidsAge: Int,
    @SerialName("kidsTendency")
    val kidsTendency: String,
    @SerialName("kidsNote")
    val kidsNote: String,

    @SerialName("goal")
    val goal: String,
    @SerialName("worry")
    val worry: String,
    @SerialName("personalInformationAgree")
    val personalInformationAgree: Int,   // 1 / 0

    @SerialName("propensityTest")
    val propensityTest: List<PropensityTestRequestDto>
)

@Serializable
data class PropensityTestRequestDto(
    @SerialName("propensityTestId")
    val propensityTestId: Int,
    @SerialName("propensityTestQuestion")
    val propensityTestQuestion: String,
    @SerialName("propensityTestScore")
    val propensityTestScore: Int?
)
