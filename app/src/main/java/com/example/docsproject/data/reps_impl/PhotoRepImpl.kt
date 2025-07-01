package com.example.docsproject.data.reps_impl

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.docsproject.domain.reps.PhotoRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate

class PhotoRepositoryImpl(private val context: Context) : PhotoRepository {
    override fun savePdfDocument(context: Context, document: PdfDocument): Uri {
        try {
            val pdfFile = File(
                context.filesDir,
                "document_${System.currentTimeMillis()}_${LocalDate.now()}.pdf"
            )
            FileOutputStream(pdfFile).use { outputStream ->
                document.writeTo(outputStream)
            }
            Log.e("PDF_CREATION", pdfFile.absolutePath)
            return pdfFile.absolutePath.toUri()
        } catch (e: Exception) {
            Log.e("PDF_SAVE", "Ошибка сохранения PDF: ${e.message}")
            return "".toUri()
        } finally {
            document.close()
        }
    }

    override fun savePdfFromUri(context: Context, sourceUri: Uri): Uri {
        val pdfFile =
            File(context.filesDir, "document_${System.currentTimeMillis()}_${LocalDate.now()}.pdf")
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(pdfFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("Не удалось открыть файл: $sourceUri")

        return pdfFile.absolutePath.toUri()
    }

    override fun getFileByUri(context: Context,uri: Uri): InputStream? {
        return context.contentResolver.openInputStream(uri)
    }

    override fun deleteDocument(uri: Uri) {
        val file = File(uri.toString())
        file.delete()
    }

    override fun getAllDocuments(): List<Uri> {
        val filesDir = context.filesDir
        val files = filesDir.listFiles() ?: emptyArray()
        val uris = mutableListOf<Uri>()
        files.sortedByDescending { it.lastModified() }.forEach {
            uris.add(it.absolutePath.toUri())
        }
        Log.e("PDF_LOAD", uris.toString())
        return uris
    }

    override fun getDocument(uri: Uri): ParcelFileDescriptor {
        val file = File(uri.toString())
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw Exception("Failed to open file descriptor")
        return parcelFileDescriptor
    }
}
