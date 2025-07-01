package com.example.docsproject.domain.reps

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.InputStream

interface PhotoRepository {
    fun savePdfDocument(context: Context, document: PdfDocument): Uri
    fun getAllDocuments(): List<Uri>
    fun getDocument(uri: Uri): ParcelFileDescriptor
    fun savePdfFromUri(context: Context, sourceUri: Uri): Uri
    fun deleteDocument(uri: Uri)
    fun getFileByUri(context: Context, uri: Uri): InputStream?
}