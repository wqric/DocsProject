package com.example.docsproject.presentation.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.docsproject.presentation.ui.theme.BluePrimary
import com.example.docsproject.presentation.ui.theme.DocsProjectTheme
import com.example.docsproject.R
import com.example.docsproject.presentation.ui.theme.Background
import com.example.docsproject.presentation.ui.theme.Gray1
import com.example.docsproject.presentation.viewmodels.PhotoViewModel
import java.lang.Thread.sleep

@Composable

fun ESignScreen(
    navController: NavController,
    viewModel: PhotoViewModel,
    currentDocument: SnapshotStateList<Bitmap>,
    onTakePhotoClick: () -> Unit
) {
    var alertState by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.9f)
                .background(Background),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = "E-Sign",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .clip(shape = MaterialTheme.shapes.large)
                    .fillMaxWidth()
                    .height(330.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = MaterialTheme.shapes.large,
                        spotColor = Color.Black.copy(alpha = 0.1f),
                        ambientColor = Color.Black.copy(alpha = 0.05f)
                    )
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val context = LocalContext.current
                val pdfPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    uri?.let {
                        context.contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        val uri = viewModel.saveExternalPdf(context, uri)
                        currentDocument.addAll(viewModel.renderDocument(uri))
                        navController.navigate("document (bitmap)")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.95f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)

                            .clip(shape = MaterialTheme.shapes.large)
                            .background(Background)
                            .clickable {
                                onTakePhotoClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Camera",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Spacer(Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .clip(shape = MaterialTheme.shapes.large)
                            .background(BluePrimary)
                            .padding(end = 5.dp, top = 5.dp)
                            .clickable {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Upload",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White
                            )
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(shape = MaterialTheme.shapes.large)
                    .fillMaxWidth()
                    .height(85.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = MaterialTheme.shapes.large,
                        spotColor = Color.Black.copy(alpha = 0.1f),
                        ambientColor = Color.Black.copy(alpha = 0.05f)
                    )
                    .background(Color.White)
                    .clickable {
                        if (viewModel.uriList.toList().isNotEmpty()) {
                            navController.navigate("history")
                        } else {
                            alertState = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "View your history",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Image(
                        painter = painterResource(R.drawable.arrow),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }

            }
        }
        if (alertState) {
            AlertDialog(
                onDismissRequest = {
                    alertState = false
                },
                title = { Text("Your history is empty!") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            alertState = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                }
            )
        }
    }
}


