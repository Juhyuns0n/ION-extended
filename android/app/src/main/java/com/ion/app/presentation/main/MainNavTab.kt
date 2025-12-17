package com.ion.app.presentation.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.ion.app.R
import com.ion.app.R.string.ic_home_desc
import com.ion.app.R.string.ic_voice_desc
import com.ion.app.R.string.ic_workbook_desc
import com.ion.app.R.string.ic_chatbot_desc
import com.ion.app.core.navigation.MainTabRoute
import com.ion.app.core.navigation.Route
import com.ion.app.presentation.chatbot.navigation.Chatbot
import com.ion.app.presentation.home.navigation.Home
import com.ion.app.presentation.voicescreen.navigation.Voice
import com.ion.app.presentation.workbook.navigation.Workbook


enum class MainNavTab(
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int,
    val route: MainTabRoute
) {
    HOME(
        icon = R.drawable.ic_home,
        contentDescription = ic_home_desc,
        route = Home
    ),
    VOICE(
        icon = R.drawable.ic_voice,
        contentDescription = ic_voice_desc,
        route = Voice
    ),
    WORKBOOK(
        icon = R.drawable.ic_workbook,
        contentDescription = ic_workbook_desc,
        route = Workbook
    ),
    CHATBOT(
        icon = R.drawable.ic_chatbot,
        contentDescription = ic_chatbot_desc,
        route = Chatbot
    );

    companion object {
        @Composable
        fun find(predicate: @Composable (MainTabRoute) -> Boolean): MainNavTab? {
            return entries.find { predicate(it.route) }
        }

        @Composable
        fun contains(predicate: @Composable (Route) -> Boolean): Boolean {
            return entries.map { it.route }.any { predicate(it) }
        }
    }
}
