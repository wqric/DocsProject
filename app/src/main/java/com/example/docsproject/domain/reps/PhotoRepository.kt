package com.example.docsproject.domain.reps

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.InputStream

interface PhotoRepository {
    fun isFileNameExists(context: Context, fileName: String): Boolean
    fun savePdfDocument(context: Context, document: PdfDocument, name: String): Uri
    fun getDocumentByUri(context: Context, uri: Uri): File
    fun getAllDocuments(): List<Uri>
    fun getDocument(uri: Uri): ParcelFileDescriptor
    fun savePdfFromUri(context: Context, sourceUri: Uri): Uri
    fun deleteDocument(uri: Uri)
    fun getFileByUri(context: Context, uri: Uri): InputStream?
}