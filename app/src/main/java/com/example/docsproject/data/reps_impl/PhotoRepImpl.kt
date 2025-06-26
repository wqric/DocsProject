package com.example.docsproject.data.reps_impl

import android.content.Context
import android.util.Log
import com.example.docsproject.domain.reps.PhotoRepository
import java.io.File
import java.time.LocalDate

class PhotoRepositoryImpl(private val context: Context) : PhotoRepository {
    override fun createPhotoFile(): File {
        val fileName = "photo_${System.currentTimeMillis()}_${LocalDate.now()}.jpg"
        val dir = File(context.filesDir, "images")
        if (!dir.exists()) dir.mkdir()
        return File(dir, fileName)
    }

    override fun getAllPhotos(): List<File> {
        val dir = File(context.filesDir, "images")

        if (!dir.exists()) dir.mkdir()
        val list = mutableListOf<File>()
        dir.list()?.forEachIndexed { i, it ->
            list.add(File("${context.filesDir}/images", it))
        }
        return list
    }
}
