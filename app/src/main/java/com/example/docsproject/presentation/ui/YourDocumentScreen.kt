package com.example.docsproject.presentation.ui

import android.content.ActivityNotFoundException
import android.content.Context
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.docsproject.R
import com.example.docsproject.presentation.ui.theme.Background
import com.example.docsproject.presentation.ui.theme.OrangePrimary
import com.example.docsproject.presentation.viewmodels.PhotoViewModel
import kotlinx.coroutines.launch

@Composable
fun YourDocumentScreen(
    navController: NavController,
    viewModel: PhotoViewModel,
) {
    DisposableEffect({}) {
        onDispose {
            viewModel.pageStates.clear()
            viewModel.documents.clear()
        }
    }
    viewModel.initPageStates()
    val showDialog = remember { mutableStateOf(false) }
    var composeCanvasSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current.density
    val context = LocalContext.current
    val text = remember { mutableStateOf("") }
    val writeText = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val pager = rememberPagerState { viewModel.documents.size }
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
                            viewModel.documents.clear()
                            navController.popBackStack()
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
            viewModel.documents.forEachIndexed { i, _ ->
                viewModel.pageStates.put(
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
            val isDrawing = viewModel.pageStates.getValue(pager.currentPage).isDrawing
            var isZoomEnabled by viewModel.pageStates.getValue(pager.currentPage).isZoomEnabled
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(shape = MaterialTheme.shapes.large),
            ) {
                viewModel.documents.forEach {
                    viewModel.currentPdfBitmaps.add(viewModel.getBitmapByUri(context, it))
                }
                Log.d("map", viewModel.pageStates.toString())
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    userScrollEnabled = false
                ) {
                    var scale by viewModel.pageStates.getValue(pager.currentPage).scale
                    var offsetX by viewModel.pageStates.getValue(pager.currentPage).offsetX
                    var offsetY by viewModel.pageStates.getValue(pager.currentPage).offsetY
                    var drawingPath = viewModel.pageStates.getValue(pager.currentPage).drawingPath
                    Box(
                        modifier = Modifier
                            .width(viewModel.currentPdfBitmaps[pager.currentPage].width.dp)
                            .height(viewModel.currentPdfBitmaps[pager.currentPage].height.dp)
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
                        AsyncImage(
                            model = viewModel.currentPdfBitmaps[pager.currentPage],
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

                        SignCanvas(
                            drawingPath, isDrawing = isDrawing, modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned { coordinates ->
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
            var enabled by remember { mutableStateOf(false) }
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    isZoomEnabled = !isZoomEnabled
                    isDrawing.value = !isDrawing.value
                    enabled = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                )
            ) {
                Text(
                    text = if (isDrawing.value) "Stop" else "Draw",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White
                    )
                )

            }

            val context = LocalContext.current
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    isDrawing.value = false
                    writeText.value = true
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                ),
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White
                    )
                )
            }
        }
        if (writeText.value) {
            val isError = remember { mutableStateOf(false) }
            Dialog(
                onDismissRequest = {
                    navController.popBackStack()
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
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "Write name for document",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 18.sp
                            )
                        )
                        Spacer(Modifier.height(40.dp))
                        BasicTextField(
                            value = text.value,
                            onValueChange = { text.value = it },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(bottom = 4.dp)
                                .wrapContentHeight(),
                            singleLine = true,
                            decorationBox = { innerTextField ->

                                Column {

                                    innerTextField()

                                    Spacer(Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(if (isError.value) Color.Red else Color.Black)
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(30.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Spacer(Modifier.width(10.dp))
                            Button(
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangePrimary
                                ),
                                onClick = {
                                    writeText.value = false
                                }) {
                                Text(
                                    text = "Back",
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
                                enabled = text.value != "",
                                onClick = {
                                    if (viewModel.isFileNameExists(context, text.value)) {
                                        isError.value = true
                                        Toast.makeText(
                                            context, "file with this name is already exists",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        isError.value = false
                                        val bitmapsPaths = mutableListOf<Path>()
                                        val bitmapsToSave = mutableListOf<Bitmap>()
                                        viewModel.pageStates.values.forEachIndexed { index, it ->
                                            bitmapsToSave.add(
                                                viewModel.transformPathOnBitmap(
                                                    originalBitmap = viewModel.currentPdfBitmaps[index],
                                                    composeCanvasSize = composeCanvasSize,
                                                    density = density,
                                                    paths = it.drawingPath
                                                )
                                            )
                                            bitmapsPaths.addAll(it.drawingPath)
                                        }
                                        viewModel.saveBitmapsToPdf(context, bitmapsToSave, text.value)
                                        viewModel.pageStates.clear()
                                        showDialog.value = true

                                    }
                                }) {
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        color = OrangePrimary
                                    )
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                        }
                    }
                }
            }
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
                                containerColor = OrangePrimary
                            ),
                            onClick = {
                                navController.popBackStack()
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, viewModel.currentPdf.value)
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
                                    Toast.makeText(
                                        context,
                                        "Нет доступных приложений",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
    viewModel: PhotoViewModel
) {
    DisposableEffect({}) {
        onDispose {
            viewModel.pageStates.clear()
            viewModel.updateData()
        }
    }
    val text = remember { mutableStateOf("") }
    val writeText = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
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
        val pager = rememberPagerState { viewModel.currentPdfBitmaps.size }
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
                            viewModel.currentPdfBitmaps.clear()
                            viewModel.uriList.clear()
                            viewModel.updateData()
                            navController.popBackStack()
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
            if (viewModel.currentPdfBitmaps.isNotEmpty()) {
                viewModel.currentPdfBitmaps.forEachIndexed { i, _ ->
                    viewModel.pageStatesBitmap.put(
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
            val isDrawing = viewModel.pageStatesBitmap.getValue(pager.currentPage).isDrawing
            var isZoomEnabled by viewModel.pageStatesBitmap.getValue(pager.currentPage).isZoomEnabled

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {

                Log.d("map", viewModel.pageStatesBitmap.toString())
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    userScrollEnabled = false
                ) {
                    var scale by viewModel.pageStatesBitmap.getValue(pager.currentPage).scale
                    var offsetX by viewModel.pageStatesBitmap.getValue(pager.currentPage).offsetX
                    var offsetY by viewModel.pageStatesBitmap.getValue(pager.currentPage).offsetY
                    var drawingPath =
                        viewModel.pageStatesBitmap.getValue(pager.currentPage).drawingPath
                    val bitmap = viewModel.currentPdfBitmaps[pager.currentPage]
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


                        SignCanvas(
                            drawingPath, isDrawing = isDrawing, modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned { coordinates ->
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
            var saveEnabled by remember { mutableStateOf(false) }
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    isZoomEnabled = !isZoomEnabled
                    isDrawing.value = !isDrawing.value
                    saveEnabled = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                )
            ) {
                Text(
                    text = if (isDrawing.value) "Stop" else "Draw",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White
                    )
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (text.value == "") {
                        writeText.value = true
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                ),
                enabled = saveEnabled
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White
                    )
                )
            }
        }
        if (writeText.value) {
            val isError = remember { mutableStateOf(false) }
            Dialog(
                onDismissRequest = {
                    navController.popBackStack()
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
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "Write name for document",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 18.sp
                            )
                        )
                        Spacer(Modifier.height(40.dp))
                        BasicTextField(
                            value = text.value,

                            onValueChange = { text.value = it },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(bottom = 4.dp)
                                .wrapContentHeight(),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Column {
                                    innerTextField()
                                    Spacer(Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(if (isError.value) Color.Red else Color.Black)
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(30.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Spacer(Modifier.width(10.dp))
                            Button(
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangePrimary
                                ),
                                onClick = {
                                    writeText.value = false
                                }) {
                                Text(
                                    text = "Back",
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
                                enabled = text.value != "",
                                onClick = {
                                    if (viewModel.isFileNameExists(context, text.value)) {
                                        isError.value = true
                                        Toast.makeText(
                                            context, "file with this name already exists",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        isError.value = false
                                        val bitmapsPaths = mutableListOf<Path>()
                                        val bitmapsToSave = mutableListOf<Bitmap>()
                                        viewModel.pageStatesBitmap.values.forEachIndexed { index, it ->
                                            bitmapsToSave.add(
                                                viewModel.transformPathOnBitmap(
                                                    originalBitmap = viewModel.currentPdfBitmaps[index],
                                                    composeCanvasSize = composeCanvasSize,
                                                    density = density,
                                                    paths = it.drawingPath
                                                )
                                            )
                                            bitmapsPaths.addAll(it.drawingPath)
                                        }
                                        if (bitmapsPaths.isNotEmpty()) {
                                            viewModel.currentPdf.value =
                                                viewModel.saveBitmapsToPdf(context, bitmapsToSave, text.value)
                                        }
                                        viewModel.pageStates.clear()
                                        showDialog.value = true
                                    }
                                }) {
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        color = OrangePrimary
                                    )
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                        }
                    }
                }
            }
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
                                containerColor = OrangePrimary
                            ),
                            onClick = {
                                navController.popBackStack()
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, viewModel.currentPdf.value)
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
                                    Toast.makeText(
                                        context,
                                        "Нет доступных приложений",
                                        Toast.LENGTH_SHORT
                                    ).show()
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


@Composable
fun SignCanvas(
    paths: SnapshotStateList<Path>,
    modifier: Modifier = Modifier,
    isDrawing: MutableState<Boolean>
) {
    var currentPath by remember { mutableStateOf<Path>(Path()) }
    var currentPoints by remember { mutableStateOf(0) }
    val drawColor = Color.Black
    val strokeWidth = 4.dp
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(isDrawing.value) {
                if (isDrawing.value) {
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



