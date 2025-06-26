package com.example.docsproject.domain.use_cases

import com.example.docsproject.domain.reps.PhotoRepository
import java.io.File

class PhotoUseCase(private val photoRepository: PhotoRepository) {
    fun savePhoto(): File {
        return photoRepository.createPhotoFile()
    }
    fun getAllPhotos(): List<File> {
        return photoRepository.getAllPhotos()
    }
}