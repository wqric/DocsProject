package com.example.docsproject.presentation.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.docsproject.R
import com.example.docsproject.presentation.ui.theme.BluePrimary
import com.example.docsproject.presentation.ui.theme.EmptyIcon

@Composable
fun YourDocumentScreen(visible: MutableState<Boolean>, photoUri: Uri?) {
    CustomPartialBottomSheet(visible) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.98f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.cross),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            visible.value = false
                        }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Your Document",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Box(modifier = Modifier.size(36.dp).padding(end = 12.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.edit), contentDescription = null,
                        modifier = Modifier.size(24.dp)

                    )
                }

            }
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(shape = MaterialTheme.shapes.large)
                    .background(EmptyIcon)
            ) {
                photoUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Фото",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.FillHeight
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.cross), contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Page 1",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.edit), contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }
    }
}