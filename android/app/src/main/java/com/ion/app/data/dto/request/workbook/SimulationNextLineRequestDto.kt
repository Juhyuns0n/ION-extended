package com.ion.app.data.dto.request.workbook

import kotlinx.serialization.Serializable

@Serializable
data class SimulationNextLineRequestDto(
    val dialogues: List<DialogueDto>
) {
    @Serializable
    data class DialogueDto(
        val userLine: String?,   // 사용자가 한 말 (없을 수도 있어서 nullable)
        val aiLine: String?      // 아이가 한 말
    )
}
