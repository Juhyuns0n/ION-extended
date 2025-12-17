package com.ion.app.domain.model.auth

data class MembershipResultModel(
    val listScores: List<Map<Int, Int>>,
    val userType: String,
    val typeExplain: String,
    val testScores: List<TestScoreModel>,
    val subtype: List<SubtypeModel>
)

data class TestScoreModel(
    val authoritative: Double,
    val authoritarian: Double,
    val permissive: Double
)

data class SubtypeModel(
    val type: String,
    val score: Double
)

data class MembershipRequestModel(
    val email: String,
    val password: String,
    val parentName: String,
    val parentNickname: String,
    val kidsNickname: String,
    val kidsAge: Int,
    val kidsTendency: String,
    val kidsNote: String,
    val goal: String,
    val worry: String,
    val personalInformationAgree: Int, // 1 or 0
    val propensityTest: List<PropensityTestItemModel>
)

data class MembershipResponseModel(
    val userId: Long
)

data class PropensityTestItemModel(
    val propensityTestId: Int,
    val propensityTestQuestion: String,
    val propensityTestScore: Int?
)