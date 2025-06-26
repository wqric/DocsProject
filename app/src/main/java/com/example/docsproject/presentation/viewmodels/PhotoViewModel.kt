package com.example.docsproject.presentation.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.example.docsproject.domain.use_cases.PhotoUseCase
import java.io.File

class PhotoViewModel(
    private val photoUseCase: PhotoUseCase
): ViewModel() {
    private val _photoUri = mutableStateOf<Uri?>(null)
    val photoUri = _photoUri
    private var currentFile: File? = null

    fun preparePhotoFile(context: Context): Uri {
        val file = photoUseCase.savePhoto()
        currentFile = file
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    fun onPhotoTaken(success: Boolean) {
        if (success) {
            _photoUri.value = currentFile?.let { Uri.fromFile(it) }

        }
    }

    fun getAllPhotos(context: Context): List<Uri> {
        val listOfFiles = photoUseCase.getAllPhotos()
        val listOfUri = mutableListOf<Uri>()
        listOfFiles.forEach {
            listOfUri.add(FileProvider.getUriForFile(context, "${context.packageName}.provider", it))
        }

        return listOfUri
    }
}