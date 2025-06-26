package com.example.docsproject.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.docsproject.presentation.ui.theme.Background
import com.example.docsproject.R
import com.example.docsproject.presentation.ui.theme.TextPrimary

@Composable
@Preview
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(start = 24.dp)
        )
        Spacer(Modifier.height(20.dp))
        data class MenuItem(
            val title: String,
            val iconResId: Int,
            val onClick: () -> Unit = {}
        )

        val menuItems = listOf(
            MenuItem("Feedback", R.drawable.feedback),
            MenuItem("Rate us", R.drawable.rate_us),
            MenuItem("Privacy Policy", R.drawable.privacy_policy),
            MenuItem("Terms of use", R.drawable.terms_of_use),
            MenuItem("Share app", R.drawable.share_app)
        )
        Column {
            menuItems.forEach { item ->
                SettingsRow(
                    title = item.title,
                    iconResId = item.iconResId,
                    onClick = item.onClick
                )
            }
        }
    }
}


@Composable
fun SettingsRow(
    title: String,
    iconResId: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.padding(start = 24.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.weight(1f))
        Icon(
            painter = painterResource(R.drawable.arrow_2),
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.padding(end = 24.dp)
        )
    }
}