package com.example.docsproject.presentation.viewmodels

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.View
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.unit.IntSize
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.example.docsproject.domain.use_cases.PhotoUseCase
import java.io.File
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withMatrix
import com.example.docsproject.presentation.ui.PageState

class PhotoViewModel(
    private val photoUseCase: PhotoUseCase
) : ViewModel() {
    val uriList = mutableStateListOf<Uri>()
    val map = mutableMapOf<Int, PageState>()
    fun saveDocuments(context: Context, list: List<Uri>) {
        photoUseCase.saveDocuments(context, list)
    }

    fun updateData() {
        uriList.clear()
        getAllDocuments()
    }

    fun saveExternalPdf(context: Context, uri: Uri): Uri {
        return photoUseCase.saveExternalPdf(context, uri)
    }

    fun getAllDocuments() {
        val list = mutableListOf<Uri>()
        val files = photoUseCase.getAllDocuments()
        files.forEach {
            if (it.toString().contains(".pdf")) {
                list.add(it)
            }
        }
        uriList.clear()
        uriList.addAll(list)
    }

    fun renderDocument(uri: Uri, pageIndex: Int = -1): List<Bitmap> {
        return photoUseCase.renderDocument(uri, pageIndex)
    }

    fun getBitmapByUri(context: Context, uri: Uri): Bitmap {
        return photoUseCase.getBitmapByUri(context, uri)
    }

    fun deleteDocument(uri: Uri) {
        photoUseCase.deleteDocument(uri)
    }

    fun saveBitmapsToPdf(context: Context, bitmaps: List<Bitmap>): Uri {
        return photoUseCase.saveBitmapsToPdf(context, bitmaps)
    }

    fun transformPathOnBitmap(
        originalBitmap: Bitmap,
        paths: List<Path>,
        composeCanvasSize: IntSize, // Добавляем размеры Compose Canvas
        density: Float // Добавляем плотность экрана
    ): Bitmap {
        // Создаем изменяемую копию
        val resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true) ?: createBitmap(
            originalBitmap.width,
            originalBitmap.height
        )

        val canvas = Canvas(resultBitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 2f * density
            color = android.graphics.Color.BLACK
        }

        // Рисуем исходный Bitmap
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        // Применяем матрицу преобразования
        val matrix = Matrix().apply {
            // Масштабирование из dp в пиксели
            val scaleX = originalBitmap.width.toFloat() / composeCanvasSize.width
            val scaleY = originalBitmap.height.toFloat() / composeCanvasSize.height
            postScale(scaleX, scaleY)
        }

        paths.forEach { path ->
            val transformedPath = AndroidPath(path.asAndroidPath()).apply {
                transform(matrix) // Применяем преобразование
            }
            canvas.drawPath(transformedPath, paint)
        }

        return resultBitmap
    }


}