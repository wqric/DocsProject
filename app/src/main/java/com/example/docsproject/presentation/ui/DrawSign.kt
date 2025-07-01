package com.example.docsproject.presentation.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.docsproject.R
import com.example.docsproject.presentation.ui.theme.BluePrimary
import com.example.docsproject.presentation.ui.theme.Gray1
import kotlinx.coroutines.launch

@Composable
fun DrawSignScreen(navController: NavController, paths: SnapshotStateList<Path>) {
    BackHandler {
        navController.navigate("document")
        paths.clear()
    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.95f)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .rotate(180f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            navController.navigate("E-sign")
                        }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Draw your sign",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.cross),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            navController.navigate("E-sign")
                        }
                )
            }
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                SignCanvas(paths)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    paths.clear()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gray1
                )
            ) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.displayMedium
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    navController.navigate("document with stamp")
                    Log.d("paths", paths.toList().toString())
                },
                enabled = !paths.isEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Text(
                    text = "Add Stump",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun DrawSignScreenBitmap(navController: NavController, paths: SnapshotStateList<Path>) {
    BackHandler {
        navController.navigate("document (bitmap)")
        paths.clear()
    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.95f)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .rotate(180f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            navController.navigate("E-sign")
                        }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Draw your sign",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.cross),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            navController.navigate("E-sign")
                        }
                )
            }
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                SignCanvas(paths)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    paths.clear()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gray1
                )
            ) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.displayMedium
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    navController.navigate("document with stamp (bitmap)")
                    Log.d("paths", paths.toList().toString())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !paths.isEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Text(
                    text = "Add Stump",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White
                    )
                )
            }
        }
    }
}

