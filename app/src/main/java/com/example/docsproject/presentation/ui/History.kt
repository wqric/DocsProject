package com.example.docsproject.presentation.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.docsproject.R
import com.example.docsproject.presentation.ui.theme.EmptyIcon
import com.example.docsproject.presentation.ui.theme.Gray2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.disk.DiskCache
import com.example.docsproject.presentation.ui.theme.Background
import com.example.docsproject.presentation.viewmodels.PhotoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Thread.sleep

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: PhotoViewModel,
) {

    DisposableEffect({}) {
        onDispose {
            viewModel.uriList.clear()
        }
    }
    var state by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .clip(shape = MaterialTheme.shapes.large)
                .fillMaxHeight()
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(15.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(180f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            navController.navigate("E-sign")
                        }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Sign History",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1.1f))

            }
            if (state) {
                AlertDialog(
                    onDismissRequest = {
                        navController.navigate("E-sign")
                    },
                    title = { Text("Your history is empty!") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                navController.navigate("E-sign")
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                    }
                )
            }
            Spacer(Modifier.height(25.dp))
            LazyColumn(verticalArrangement = Arrangement.SpaceBetween) {
                items(viewModel.uriList) { uri ->
                    var showDialog by remember { mutableStateOf(false) }
                    var visible by remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HistoryCard(
                            date = uri.toString()
                                .substringAfterLast("/")
                                .substringBeforeLast("."),
                            uri = uri,
                            viewModel = viewModel,
                            navController = navController,

                            )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            painter = painterResource(R.drawable.basket),
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                showDialog = true
                            }
                        )
                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                title = { Text("Delete confirm") },
                                text = { Text("Are you sure?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showDialog = false
                                            visible = false
                                            viewModel.uriList.remove(uri)
                                            if (viewModel.uriList.isEmpty()) {
                                                state = true
                                            }
                                            viewModel.deleteDocument(uri)
                                        }
                                    ) {
                                        Text("Delete", color = Color.Red)
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showDialog = false }
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                }
            }
        }

    }
}


@Composable
fun HistoryCard(
    date: String,
    navController: NavController,
    uri: Uri,
    viewModel: PhotoViewModel
) {
    val navigationLambda: () -> Unit = remember {
        {
            viewModel.currentPdfBitmaps.addAll(viewModel.renderDocument(uri))
            navController.navigate("document (bitmap)")
        }
    }

    Column(
        modifier = Modifier
            .height(150.dp)
            .width(250.dp)
            .clip(shape = MaterialTheme.shapes.large)
            .background(Color.White)
            .clickable {
                viewModel.currentPdf.value = uri
                navigationLambda()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(10.dp))
        Image(
            painter = painterResource(R.drawable.pdf_file_type),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = date,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(start = 12.dp)
        )
        Spacer(Modifier.weight(1f))
    }
}

