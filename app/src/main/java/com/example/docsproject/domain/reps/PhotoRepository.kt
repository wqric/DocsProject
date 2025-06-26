package com.example.docsproject.domain.reps

import java.io.File

interface PhotoRepository {
    fun createPhotoFile(): File
    fun getAllPhotos(): List<File>
}