package com.example.docsproject.presentation.ui

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.docsproject.R
import com.example.docsproject.presentation.ui.theme.BluePrimary
import com.example.docsproject.presentation.viewmodels.PhotoViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DocumentWithStampScreen(
    navController: NavController,
    paths: SnapshotStateList<Path>,
    photoUriList: SnapshotStateList<Uri>,
    viewModel: PhotoViewModel
) {

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        val pager = rememberPagerState { photoUriList.size }
        val coroutineScope = rememberCoroutineScope()
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
                    painter = painterResource(R.drawable.cross),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            photoUriList.clear()
                            navController.navigate("E-sign")
                        }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Your Document",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.apply),
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
            val map = mutableMapOf<Int, PageState1>()
            for (i in 0..pager.pageCount.toInt()) {
                map.put(
                    i, PageState1(
                    rotationAngle = remember { mutableStateOf(0f) },
                    scale = remember { mutableStateOf(1f) },
                    offset = remember { mutableStateOf(Offset.Zero) }
                ))
            }
            HorizontalPager(
                state = pager,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { pageIndex ->

                val rotationAngle = map.getValue(pager.currentPage).rotationAngle
                val scale = map.getValue(pager.currentPage).scale
                val offset = map.getValue(pager.currentPage).offset

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(shape = MaterialTheme.shapes.large)
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                                    rotationAngle.value += gestureRotate
                                    scale.value *= gestureZoom
                                    offset.value += pan
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = photoUriList[pager.currentPage],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    MoveCanvasWithSign(
                        paths = paths,
                        rotationAngle = rotationAngle,
                        scale = scale,
                        offset = offset
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow), contentDescription = null,
                    modifier = Modifier
                        .rotate(180f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = pager.currentPage > 0
                        ) {
                            coroutineScope.launch {
                                pager.scrollToPage(pager.currentPage - 1)
                            }
                        }
                )
                Spacer(Modifier.weight(1f))
                Column {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "Page ${pager.currentPage + 1}",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.arrow), contentDescription = null,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = pager.currentPage < pager.pageCount
                        ) {
                            coroutineScope.launch {
                                pager.scrollToPage(pager.currentPage + 1)
                            }
                        }
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun DocumentWithStampScreenBitmap(
    navController: NavController,
    paths: SnapshotStateList<Path>,
    currentDocument: SnapshotStateList<Bitmap>,
    viewModel: PhotoViewModel
) {
    DisposableEffect({}) {
        onDispose {
            paths.clear()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        val pager = rememberPagerState { currentDocument.size }
        val coroutineScope = rememberCoroutineScope()
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
                    painter = painterResource(R.drawable.cross),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            currentDocument.clear()
                            navController.navigate("E-sign")
                        }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Your Document",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.apply),
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
            val map = mutableMapOf<Int, PageState1>()
            for (i in 0..pager.pageCount.toInt()) {
                map.put(
                    i, PageState1(
                        rotationAngle = remember { mutableStateOf(0f) },
                        scale = remember { mutableStateOf(1f) },
                        offset = remember { mutableStateOf(Offset.Zero) }
                    ))
            }
            HorizontalPager(
                state = pager,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { pageIndex ->

                val rotationAngle = map.getValue(pager.currentPage).rotationAngle
                val scale = map.getValue(pager.currentPage).scale
                val offset = map.getValue(pager.currentPage).offset

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(shape = MaterialTheme.shapes.large)
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                                    rotationAngle.value += gestureRotate
                                    scale.value *= gestureZoom
                                    offset.value += pan
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentDocument[pager.currentPage],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    MoveCanvasWithSign(
                        paths = paths,
                        rotationAngle = rotationAngle,
                        scale = scale,
                        offset = offset
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow), contentDescription = null,
                    modifier = Modifier
                        .rotate(180f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = pager.currentPage > 0
                        ) {
                            coroutineScope.launch {
                                pager.scrollToPage(pager.currentPage - 1)
                            }
                        }
                )
                Spacer(Modifier.weight(1f))
                Column {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "Page ${pager.currentPage + 1}",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.arrow), contentDescription = null,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = pager.currentPage < pager.pageCount
                        ) {
                            coroutineScope.launch {
                                pager.scrollToPage(pager.currentPage + 1)
                            }
                        }
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White
                    )
                )
            }
        }
    }
}

data class PageState1(
    val rotationAngle: MutableState<Float>,
    val scale: MutableState<Float>,
    val offset: MutableState<Offset>
)

@Composable
fun MoveCanvasWithSign(
    visible: Boolean = true,
    paths: SnapshotStateList<Path>,
    rotationAngle: MutableState<Float>,
    scale: MutableState<Float>,
    offset: MutableState<Offset>
) {
    val imageSize = remember {
        mutableStateOf(
            Size(
                getCombinedPathsBounds(paths).width().toFloat(),
                getCombinedPathsBounds(paths).height().toFloat()
            )
        )
    }


    val frameColor = Color.Blue.copy(alpha = 0.5f)
    val handleColor = Color.Red
    val handleSize = 20.dp
    val strokeWidth = 2.dp
    Box(
        modifier = Modifier
            .size(
                imageSize.value.width.dp,
                imageSize.value.height.dp
            )
            .offset {
                IntOffset(
                    offset.value.x.roundToInt(),
                    offset.value.y.roundToInt()
                )
            }
            .rotate(rotationAngle.value)
            .scale(scale.value),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            paths.forEach {
                drawPath(
                    path = it,
                    color = Color.Black,
                    style = Stroke(5f)
                )
            }
        }
        if (visible) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pathBounds = calculatePathsBounds(paths)
                if (pathBounds != Rect.Zero && visible) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = pathBounds.topLeft,
                        size = pathBounds.size
                    )
                    drawRect(
                        color = frameColor,
                        topLeft = pathBounds.topLeft,
                        size = pathBounds.size,
                        style = Stroke(width = strokeWidth.toPx())
                    )
                    val handleRadius = handleSize.toPx() / 2
                    val corners = listOf(
                        pathBounds.topLeft,
                        pathBounds.topRight,
                        pathBounds.bottomRight,
                        pathBounds.bottomLeft
                    )

                    corners.forEach { corner ->
                        drawCircle(
                            color = handleColor,
                            radius = handleRadius,
                            center = corner
                        )
                    }
                }
            }
        }
    }
}


private fun calculatePathsBounds(paths: List<Path>): Rect {

    var left = Float.POSITIVE_INFINITY
    var top = Float.POSITIVE_INFINITY
    var right = Float.NEGATIVE_INFINITY
    var bottom = Float.NEGATIVE_INFINITY

    paths.forEach { path ->
        val bounds = path.getBounds()
        left = minOf(left, bounds.left)
        top = minOf(top, bounds.top)
        right = maxOf(right, bounds.right)
        bottom = maxOf(bottom, bounds.bottom)
    }

    return Rect(left, top, right, bottom)
}

fun getCombinedPathsBounds(paths: List<Path>): RectF {
    val combinedBounds = RectF()
    val tempBounds = RectF()

    if (paths.isEmpty()) return combinedBounds

    // Инициализируем первым path
    paths[0].asAndroidPath().computeBounds(tempBounds, true)
    combinedBounds.set(tempBounds)

    // Объединяем с остальными paths
    for (i in 1 until paths.size) {
        paths[i].asAndroidPath().computeBounds(tempBounds, true)
        combinedBounds.union(tempBounds)
    }

    return combinedBounds
}