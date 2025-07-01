package com.example.docsproject.domain.use_cases

import android.content.Context
import android.graphics.*
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import com.example.docsproject.domain.reps.PhotoRepository
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.math.min
import androidx.core.graphics.scale

class PhotoUseCase(private val photoRepository: PhotoRepository) {
    fun saveDocuments(context: Context, uris: List<Uri>, name: String) {
        try {
            val document = PdfDocument()
            val bitmaps = mutableListOf<Bitmap>()
            uris.forEach {
                bitmaps.add(BitmapFactory.decodeStream(photoRepository.getFileByUri(context, it)))
            }
            bitmaps.forEachIndexed { index, bitmap ->
                val pageInfo = PdfDocument.PageInfo.Builder(
                    bitmap.width,
                    bitmap.height,
                    index + 1
                ).create()

                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)
            }
            photoRepository.savePdfDocument(context, document, name)

        } catch (e: Exception) {
            Log.e("PDF_CREATION", "Ошибка создания PDF: ${e.message}")
        }
    }
    fun getDocument(context: Context, uri: Uri): File {
        return photoRepository.getDocumentByUri(context, uri)
    }

    fun deleteDocument(uri: Uri) {
        photoRepository.deleteDocument(uri)
    }

    fun saveExternalPdf(context: Context, uri: Uri): Uri {
        return photoRepository.savePdfFromUri(context, uri)
    }

    fun getBitmapByUri(context: Context, uri: Uri): Bitmap {
        val inputStream = photoRepository.getFileByUri(context, uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun compressBitmap(original: Bitmap, maxDimension: Int = 2400, quality: Int = 80): Bitmap {
        val scale = min(
            maxDimension.toFloat() / original.width,
            maxDimension.toFloat() / original.height
        ).coerceAtMost(1f)

        val width = (original.width * scale).toInt()
        val height = (original.height * scale).toInt()

        return original.scale(width, height).apply {
            val byteArray = ByteArrayOutputStream().use { stream ->
                this.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                stream.toByteArray()
            }
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }
    }
    fun isFileNameExists(context: Context, fileName: String): Boolean {
        return photoRepository.isFileNameExists(context, fileName)
    }
    fun saveBitmapsToPdf(context: Context, bitmaps: List<Bitmap>, name: String): Uri {
        try {
            val document = PdfDocument()
            bitmaps.forEachIndexed { index, bitmap ->
                val bit = compressBitmap(bitmap)
                val pageInfo = PdfDocument.PageInfo.Builder(
                    bit.width,
                    bit.height,
                    index + 1
                ).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawBitmap(bit, 0f, 0f, null)
                document.finishPage(page)
                bit.recycle()
            }
            return photoRepository.savePdfDocument(context, document, name)
        } catch (e: Exception) {
            Log.e("PDF_CREATION", "Ошибка создания PDF: ${e.message}")
            return "".toUri()
        }
    }

    fun getAllDocuments(): List<Uri> {
        return photoRepository.getAllDocuments()
    }

    fun renderDocument(uri: Uri, pageIndex: Int = -1): List<Bitmap> {
        val path = uri.path
        if (path != null && !path.substringAfterLast('.', "").equals("pdf", ignoreCase = true)) {
            return emptyList()
        }
        Log.d("PDF_RENDER", "Rendering document: $uri")
        val parcelFileDescriptor = photoRepository.getDocument(uri)
        val pdfRenderer: PdfRenderer = try {
            PdfRenderer(parcelFileDescriptor)
        } catch (e: IOException) {
            Log.e(
                "PDF_RENDER",
                "IOException creating PdfRenderer for $uri. File might be corrupted or not a PDF.",
                e
            )
            parcelFileDescriptor.close()
            throw IOException("Invalid PDF file: $uri", e)
        } catch (e: SecurityException) {
            Log.e("PDF_RENDER", "SecurityException creating PdfRenderer", e)
            parcelFileDescriptor.close()
            throw SecurityException("Security exception when creating PdfRenderer", e)
        } catch (e: Exception) {
            Log.e("PDF_RENDER", "Unexpected error creating PdfRenderer", e)
            parcelFileDescriptor.close()
            throw RuntimeException("Unexpected error creating PdfRenderer", e)
        }
        val bitmapList = mutableListOf<Bitmap>()
        try {
            if (pageIndex == -1) {
                for (i in 0 until pdfRenderer.pageCount) {
                    Log.d("PDF_RENDER", "Rendering page $i of ${pdfRenderer.pageCount}")
                    renderPage(pdfRenderer, i)?.let { bitmapList.add(it) }
                }
            } else {
                if (pageIndex < 0 || pageIndex >= pdfRenderer.pageCount) {
                    throw IndexOutOfBoundsException("Page index $pageIndex out of bounds. Total pages: ${pdfRenderer.pageCount}")
                }
                renderPage(pdfRenderer, pageIndex)?.let { bitmapList.add(it) }
            }
        } catch (e: Exception) {
            Log.e("PDF_RENDER", "Error rendering document", e)
            bitmapList.forEach { it.recycle() }
            throw e
        } finally {
            pdfRenderer.close()
            parcelFileDescriptor.close()
        }
        return bitmapList
    }

    private fun renderPage(pdfRenderer: PdfRenderer, pageIndex: Int): Bitmap? {
        val page = pdfRenderer.openPage(pageIndex)
        try {
            val bitmap = createBitmap(page.width, page.height)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            return bitmap
        } catch (e: Exception) {
            Log.e("PDF_RENDER", "Error rendering page $pageIndex", e)
            return null
        } finally {
            page.close()
        }
    }
}
