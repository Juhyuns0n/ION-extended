package com.ion.app.presentation.main.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ion.app.R
import com.ion.app.core.designsystem.ui.theme.gray400
import com.ion.app.core.designsystem.ui.theme.primary300
import com.ion.app.core.util.noRippleClickable
import com.ion.app.core.util.screenHeightDp
import com.ion.app.presentation.main.MainNavTab
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MainBottomBar(
    visible: Boolean,
    tabs: ImmutableList<MainNavTab>,
    currentTab: MainNavTab?,
    onTabSelected: (MainNavTab) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        Column(
            modifier = Modifier.paint(
                painter = painterResource(R.drawable.bottom_bar),
                contentScale = ContentScale.FillBounds
            )
        ) {
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .padding(vertical = screenHeightDp(10.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEach { tab ->
                    key(tab.route) {
                        val selected = currentTab == tab
                        MainBottomBarItem(
                            tab = tab,
                            selected = selected,
                            onClick = { onTabSelected(tab) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.MainBottomBarItem(
    modifier: Modifier = Modifier,
    tab: MainNavTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bottomItemColor = if (selected) primary300 else gray400
    Column(
        modifier = modifier
            .noRippleClickable(onClick = onClick)
            .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Icon(
            painter = painterResource(tab.icon),
            contentDescription = stringResource(tab.contentDescription),
            tint = bottomItemColor
        )
        Text(
            text = stringResource(tab.contentDescription),
            fontSize = 14.sp,
            color = bottomItemColor
        )
    }
}
