package com.example.docsproject.data.reps_impl

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.docsproject.domain.reps.PhotoRepository
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate

class PhotoRepositoryImpl(private val context: Context) : PhotoRepository {
    override fun savePdfDocument(context: Context, document: PdfDocument, name: String): Uri {
        var outputStream: FileOutputStream? = null
        try {
            val safeName = name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "$safeName.pdf"
            val pdfFile = File(context.filesDir, fileName)

            outputStream = FileOutputStream(pdfFile)
            BufferedOutputStream(outputStream).use { bufferedStream ->
                document.writeTo(bufferedStream)
            }

            if (pdfFile.length() == 0L) {
                throw IOException("PDF file is empty (0 bytes)")
            }

            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, pdfFile)

            val mimeType = context.contentResolver.getType(fileUri)
                ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
                ?: "application/pdf"

            Log.d("PDF_SAVE", "PDF saved: ${pdfFile.absolutePath}")
            Log.d("PDF_SAVE", "MIME Type: $mimeType")
            Log.d("PDF_SAVE", "File size: ${pdfFile.length()} bytes") // Должно быть "application/pdf"
            return fileUri
        } catch (e: Exception) {
            Log.e("PDF_SAVE", "Ошибка сохранения PDF: ${e.message}")
            return "".toUri()
        } finally {
            document.close()
        }
    }
    override fun isFileNameExists(context: Context, fileName: String): Boolean {
        return File(context.filesDir, "$fileName.pdf").exists()
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

    override fun getDocumentByUri(context: Context, uri: Uri): File {
        return File(
            context.filesDir,
            uri.toString()
        )
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
            "${context.packageName}.fileprovider",
            file
        )
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw Exception("Failed to open file descriptor")
        return parcelFileDescriptor
    }
}
