package com.example.docsproject.presentation.ui

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.docsproject.R
import com.example.docsproject.presentation.ui.theme.EmptyIcon
import com.example.docsproject.presentation.ui.theme.Gray2


@Composable
fun CustomPartialBottomSheet(visible: MutableState<Boolean>, content: @Composable () -> Unit) {
    val offsetY = animateFloatAsState(
        targetValue = if (visible.value) 200f else 1000f,
        animationSpec = tween(durationMillis = 300)
    )
    Box(
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.large)
            .fillMaxWidth()
            .fillMaxHeight()
            .offset(y = offsetY.value.dp)
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun History(visible: MutableState<Boolean>, uriList: List<Uri>) {
    CustomPartialBottomSheet(visible) {
        val navController = rememberNavController()
        val currentDocument = remember { mutableStateOf<Uri?>(null) }
        NavHost(
            navController = navController,
            startDestination = "history"
        ) {
            composable("history") {
                Column(
                    modifier = Modifier
                        .clip(shape = MaterialTheme.shapes.large)
                        .fillMaxHeight()
                        .fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(15.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.weight(1.1f))
                        Text(
                            text = "Sign History",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            painter = painterResource(R.drawable.cross),
                            contentDescription = null,
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                visible.value = false
                            }
                        )
                    }
                    Spacer(Modifier.height(25.dp))
                    LazyColumn {
                        items(uriList.chunked(2)) {uriPair ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                HistoryCard(
                                    navController = navController,
                                    date = uriPair[0].toString()
                                        .substringAfterLast("_")
                                        .substringBeforeLast("."),
                                    image = uriPair[0],
                                    state = currentDocument
                                )
                                Spacer(Modifier.weight(1f))
                                if (uriPair.size == 2) {
                                    HistoryCard(
                                        navController = navController,
                                        date = uriPair[1].toString()
                                            .substringAfterLast("_")
                                            .substringBeforeLast("."),
                                        image = uriPair[1],
                                        state = currentDocument
                                    )
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }

                            }
                            Spacer(Modifier.height(20.dp))

                        }
                    }

                }
            }
            composable("yourDocument") {
                YourDocumentScreen(visible, currentDocument.value)
            }
        }

    }
}

@Composable
fun HistoryCard(date: String, image: Uri, navController: NavController, state: MutableState<Uri?>) {
    Column(
        modifier = Modifier
            .height(150.dp)
            .width(160.dp)
            .clip(shape = MaterialTheme.shapes.large)
            .background(EmptyIcon)
            .clickable {
                state.value = image
                navController.navigate("yourDocument")
            }
    ) {

        Image(
            painter = rememberAsyncImagePainter(image),
            contentDescription = "Фото",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentScale = ContentScale.Crop
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.arrow), contentDescription = null,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    }
}




