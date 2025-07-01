package com.example.docsproject.presentation.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import com.example.docsproject.domain.use_cases.PhotoUseCase
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.example.docsproject.presentation.ui.PageState
import java.io.File

class PhotoViewModel(
    private val photoUseCase: PhotoUseCase
) : ViewModel() {
    val documents = mutableStateListOf<Uri>()
    val uriList = mutableStateListOf<Uri>()
    val pageStates = mutableMapOf<Int, PageState>()
    val pageStatesBitmap = mutableMapOf<Int, PageState>()
    val currentPdf = mutableStateOf<Uri?>(null)
    val currentPdfBitmaps = mutableStateListOf<Bitmap>()

    fun saveExternalPdf(context: Context, uri: Uri): Uri {
        return photoUseCase.saveExternalPdf(context, uri)
    }

    fun updateData() {
        val list = mutableListOf<Uri>()
        val files = photoUseCase.getAllDocuments()
        files.forEach {
            if (it.toString().contains(".pdf")) {
                list.add(it)
            }
        }
        uriList.clear()
        Log.d("i", list.toString())
        uriList.addAll(list)
    }

    fun renderDocument(uri: Uri, pageIndex: Int = -1): List<Bitmap> {
        return photoUseCase.renderDocument(uri, pageIndex)
    }

    init {
        initPageStates()
    }
    fun initPageStates() {
        pageStates.clear()
        documents.forEachIndexed { i, _ ->
            pageStates[i] = PageState(
                scale = mutableStateOf(1f),
                offsetX = mutableStateOf(0f),
                offsetY = mutableStateOf(0f),
                currentPath = mutableStateOf(Path()),
                drawingPath = mutableStateListOf(),
                isDrawing = mutableStateOf(false),
                isZoomEnabled = mutableStateOf(true)
            )
        }
    }

    // Асинхронное получение битмапа
    fun getBitmapByUri(context: Context, uri: Uri): Bitmap = photoUseCase.getBitmapByUri(context, uri)

    fun deleteDocument(uri: Uri) {
        photoUseCase.deleteDocument(uri)
    }

    fun saveBitmapsToPdf(context: Context, bitmaps: List<Bitmap>, name: String): Uri {
        return photoUseCase.saveBitmapsToPdf(context, bitmaps, name)
    }

    fun isFileNameExists(context: Context, fileName: String): Boolean {
        return photoUseCase.isFileNameExists(context, fileName)
    }

    fun transformPathOnBitmap(
        originalBitmap: Bitmap,
        paths: List<Path>,
        composeCanvasSize: IntSize, // Добавляем размеры Compose Canvas
        density: Float
    ): Bitmap {
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


        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        // Применяем матрицу преобразования
        val matrix = Matrix().apply {
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