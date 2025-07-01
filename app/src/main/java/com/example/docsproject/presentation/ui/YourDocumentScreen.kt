package com.example.docsproject.presentation.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.docsproject.R
import com.example.docsproject.presentation.ui.theme.Background
import com.example.docsproject.presentation.ui.theme.BluePrimary
import com.example.docsproject.presentation.viewmodels.PhotoViewModel
import kotlinx.coroutines.launch
import javax.crypto.Cipher

@Composable
fun YourDocumentScreen(
    navController: NavController,
    photoUriList: SnapshotStateList<Uri>,
    viewModel: PhotoViewModel,
    map: MutableMap<Int, PageState>,

    ) {
    DisposableEffect({}) {
        onDispose {
            map.clear()
            photoUriList.clear()
        }
    }
    val showDialog = remember { mutableStateOf(false) }
    val uri = remember { mutableStateOf("".toUri()) }
    var composeCanvasSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current.density
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

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
                    painter = painterResource(R.drawable.arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .rotate(180f)
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
                    text = "Document",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 32.sp
                    ),
                )
                Spacer(Modifier.weight(1.3f))
            }
            Spacer(Modifier.height(20.dp))
            photoUriList.forEachIndexed { i, _ ->
                map.put(
                    i, PageState(
                        scale = remember { mutableStateOf(1f) },
                        offsetX = remember { mutableStateOf(0f) },
                        offsetY = remember { mutableStateOf(0f) },
                        currentPath = remember { mutableStateOf(Path()) },
                        drawingPath = remember { mutableStateListOf(Path()) },
                        isDrawing = remember { mutableStateOf(false) },
                        isZoomEnabled = remember { mutableStateOf(true) }
                    ))
            }
            Log.d("map", map[0].toString())
            Log.d("map", photoUriList.size.toString())
            Log.d("map", pager.pageCount.toString())
            Log.d("map", pager.currentPage.toString())
            var currentPath by map.getValue(pager.currentPage).currentPath
            var isDrawing by map.getValue(pager.currentPage).isDrawing
            var isZoomEnabled by map.getValue(pager.currentPage).isZoomEnabled
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(shape = MaterialTheme.shapes.large),
            ) {

                Log.d("map", map.toString())
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    userScrollEnabled = false
                ) {
                    var scale by map.getValue(pager.currentPage).scale
                    var offsetX by map.getValue(pager.currentPage).offsetX
                    var offsetY by map.getValue(pager.currentPage).offsetY
                    var drawingPath = map.getValue(pager.currentPage).drawingPath
                    val bitmap = viewModel.getBitmapByUri(context, photoUriList[pager.currentPage])
                    Box(
                        modifier = Modifier
                            .width(bitmap.width.dp)
                            .height(bitmap.height.dp)
                            .pointerInput(isZoomEnabled) {
                                if (isZoomEnabled) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    }
                                }
                            }
                    ) {
                        // Отображаем изображение с возможностью масштабирования
                        AsyncImage(
                            model = photoUriList[pager.currentPage],
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                        )


                        if (isDrawing) {
                            SignCanvas(
                                drawingPath, modifier = Modifier
                                    .fillMaxSize()
                                    .onGloballyPositioned { coordinates ->
                                        // Получаем реальные размеры в пикселях
                                        composeCanvasSize = IntSize(
                                            coordinates.size.width,
                                            coordinates.size.height
                                        )
                                    }
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                    )
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_2), contentDescription = null,
                    modifier = Modifier
                        .rotate(180f)
                        .size(24.dp)
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
                        text = "${pager.currentPage + 1} | ${pager.pageCount}",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.arrow_2), contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
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
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    isZoomEnabled = !isZoomEnabled
                    isDrawing = true
                    currentPath = Path()
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
                    text = "Start drawing",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White
                    )
                )
            }
            val context = LocalContext.current
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    isDrawing = false
                    val bitmaps = mutableListOf<Bitmap>()
                    val bitmapsPaths = mutableListOf<Path>()
                    map.values.forEachIndexed { index, it ->
                        bitmaps.add(
                            viewModel.transformPathOnBitmap(
                                originalBitmap = viewModel.getBitmapByUri(
                                    context,
                                    photoUriList[index]
                                ),
                                composeCanvasSize = composeCanvasSize,
                                density = density,
                                paths = it.drawingPath
                            )
                        )
                        bitmapsPaths.addAll(it.drawingPath)
                    }
                    if (bitmapsPaths.isNotEmpty()) {
                        uri.value = viewModel.saveBitmapsToPdf(context, bitmaps)
                    }
                    showDialog.value = true
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
        if (showDialog.value) {
            Dialog(
                onDismissRequest = {
                    navController.navigate("E-sign")
                }
            ) {
                Box(
                    modifier = Modifier
                        .width(250.dp)
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(10.dp))
                        Icon(
                            painter = painterResource(R.drawable.apply),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.Black
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Document saved",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            )
                        )
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Spacer(Modifier.width(10.dp))
                            Button(
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BluePrimary
                                ),
                                onClick = {
                                    navController.popBackStack()
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri.value)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(
                                            Intent.createChooser(
                                                shareIntent,
                                                "Отправить PDF через"
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    } catch (_: ActivityNotFoundException) {
                                        Toast.makeText(context, "Нет доступных приложений", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                Text(
                                    text = "Share",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        color = Background
                                    )
                                )
                            }

                            Spacer(Modifier.width(10.dp))
                            Button(
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),

                                onClick = {
                                    navController.popBackStack()
                                }) {
                                Text(
                                    text = "Finish",
                                    style = MaterialTheme.typography.displayMedium
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                        }
                    }
                }
            }
        }
    }
}


data class PageState(
    val scale: MutableState<Float>,
    val offsetX: MutableState<Float>,
    val offsetY: MutableState<Float>,
    val drawingPath: SnapshotStateList<Path>,
    val currentPath: MutableState<Path>,
    val isDrawing: MutableState<Boolean>,
    val isZoomEnabled: MutableState<Boolean>
)


@Composable
fun YourDocumentScreenBitmap(
    navController: NavController,
    map: MutableMap<Int, PageState>,
    bitmaps: SnapshotStateList<Bitmap>,
    pdfUri: MutableState<Uri>,
    viewModel: PhotoViewModel
) {
    DisposableEffect({}) {
        onDispose {
            viewModel.map.clear()
            viewModel.updateData()
        }
    }
    val showDialog = remember { mutableStateOf(false) }
    val uri = remember { mutableStateOf("".toUri()) }
    var composeCanvasSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current.density
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pager = rememberPagerState { bitmaps.size }
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
                    painter = painterResource(R.drawable.arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .rotate(180f)
                        .size(36.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            bitmaps.clear()
                            navController.navigate("history")
                        }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Document",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 32.sp
                    ),
                )
                Spacer(Modifier.weight(1.3f))

            }
            Spacer(Modifier.height(20.dp))
            if (bitmaps.isNotEmpty()) {
                bitmaps.forEachIndexed { i, _ ->
                    map.put(
                        i, PageState(
                            scale = remember { mutableStateOf(1f) },
                            offsetX = remember { mutableStateOf(0f) },
                            offsetY = remember { mutableStateOf(0f) },
                            currentPath = remember { mutableStateOf(Path()) },
                            drawingPath = remember { mutableStateListOf(Path()) },
                            isDrawing = remember { mutableStateOf(false) },
                            isZoomEnabled = remember { mutableStateOf(true) }
                        ))
                }
            }
            var currentPath by map.getValue(pager.currentPage).currentPath
            var isDrawing by map.getValue(pager.currentPage).isDrawing
            var isZoomEnabled by map.getValue(pager.currentPage).isZoomEnabled

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {

                Log.d("map", map.toString())
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    userScrollEnabled = false
                ) {
                    var scale by map.getValue(pager.currentPage).scale
                    var offsetX by map.getValue(pager.currentPage).offsetX
                    var offsetY by map.getValue(pager.currentPage).offsetY
                    var drawingPath = map.getValue(pager.currentPage).drawingPath
                    val bitmap = bitmaps[pager.currentPage]
                    Box(
                        modifier = Modifier
                            .width(bitmap.width.dp)
                            .height(bitmap.height.dp)
                            .pointerInput(isZoomEnabled) {
                                if (isZoomEnabled) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    }
                                }
                            }
                    ) {
                        // Отображаем изображение с возможностью масштабирования
                        AsyncImage(
                            model = bitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                        )


                        if (isDrawing) {
                            SignCanvas(
                                drawingPath, modifier = Modifier
                                    .fillMaxSize()
                                    .onGloballyPositioned { coordinates ->
                                        // Получаем реальные размеры в пикселях
                                        composeCanvasSize = IntSize(
                                            coordinates.size.width,
                                            coordinates.size.height
                                        )
                                    }
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                    )
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_2), contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
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
                        text = "${pager.currentPage + 1} | ${pager.pageCount}",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.arrow_2), contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
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
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    isZoomEnabled = !isZoomEnabled
                    isDrawing = true
                    currentPath = Path()
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
                    text = "Start drawing",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White
                    )
                )
            }
            val context = LocalContext.current
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    isDrawing = false
                    val bitmapsPaths = mutableListOf<Path>()
                    val bitmapsToSave = mutableListOf<Bitmap>()
                    map.values.forEachIndexed { index, it ->
                        bitmapsToSave.add(
                            viewModel.transformPathOnBitmap(
                                originalBitmap = bitmaps[index],
                                composeCanvasSize = composeCanvasSize,
                                density = density,
                                paths = it.drawingPath
                            )
                        )
                        bitmapsPaths.addAll(it.drawingPath)
                    }
                    if (bitmapsPaths.isNotEmpty()) {
                        Log.d("err", "${bitmapsPaths}, ${uri.value}")
                        uri.value = viewModel.saveBitmapsToPdf(context, bitmapsToSave)
                    } else {
                        uri.value = pdfUri.value
                    }
                    viewModel.map.clear()
                    showDialog.value = true
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
        if (showDialog.value) {
            Dialog(
                onDismissRequest = {
                    navController.navigate("E-sign")
                }
            ) {
                Box(
                    modifier = Modifier
                        .width(250.dp)
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(10.dp))
                        Icon(
                            painter = painterResource(R.drawable.apply),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.Black
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Document saved",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            )
                        )
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Spacer(Modifier.width(10.dp))
                            Button(
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BluePrimary
                                ),
                                onClick = {
                                    navController.popBackStack()
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri.value)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(
                                            Intent.createChooser(
                                                shareIntent,
                                                "Отправить PDF через"
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    } catch (_: ActivityNotFoundException) {
                                        Toast.makeText(context, "Нет доступных приложений", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                Text(
                                    text = "Share",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        color = Background
                                    )
                                )
                            }

                            Spacer(Modifier.width(10.dp))
                            Button(
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Background
                                ),
                                onClick = {
                                    navController.navigate("E-sign")
                                }) {
                                Text(
                                    text = "Finish",
                                    style = MaterialTheme.typography.displayMedium
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DocumentScreen(
    navController: NavController,
    photoUriList: SnapshotStateList<Uri>,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) {
                    AsyncImage(
                        photoUriList[pager.currentPage],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
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
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    onSave()
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
fun SaveDocumentScreen(
    navController: NavController,
    images: SnapshotStateList<Uri>,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val pager = rememberPagerState { images.size }
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
                            images.clear()
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) {
                    AsyncImage(
                        images[pager.currentPage],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
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
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    onSave()
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
fun SignCanvas(paths: SnapshotStateList<Path>, modifier: Modifier = Modifier) {
    var currentPath by remember { mutableStateOf<Path>(Path()) }
    var currentPoints by remember { mutableStateOf(0) }
    val drawColor = Color.Black
    val strokeWidth = 4.dp
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { touch ->

                        val newPath = Path().apply {
                            moveTo(touch.x, touch.y)
                        }
                        currentPath = newPath
                        currentPoints = 1
                    },
                    onDrag = { change, _ ->
                        currentPath.lineTo(change.position.x, change.position.y)
                        currentPoints++
                    },
                    onDragEnd = {

                        currentPath.let { paths.add(it) }
                        currentPath = Path()
                        currentPoints = 0
                    }
                )

            }
    ) {
        paths.forEach { path ->
            drawPath(
                path = path,
                color = drawColor,
                style = Stroke(width = strokeWidth.toPx())
            )
        }
        drawPath(
            path = currentPath,
            color = drawColor,
            style = Stroke(width = strokeWidth.toPx())
        )
        if (currentPoints > 0) {
            drawPath(
                path = currentPath,
                color = Color.Black,
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}



