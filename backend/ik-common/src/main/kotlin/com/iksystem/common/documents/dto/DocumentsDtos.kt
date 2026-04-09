package com.iksystem.common.documents.dto

/**
 * Response returned after a document has been successfully uploaded.
 *
 * Contains metadata about the uploaded file as well as a temporary
 * presigned URL for accessing it.
 */
data class DocumentUploadResponse(
    val id: Long,
    val fileName: String,
    val s3Key: String,
    val contentType: String,
    val url: String, // Presigned URL (temporary)
)

/**
 * Response containing a presigned URL for accessing a document.
 *
 * The URL is temporary and typically expires after a limited time.
 */
data class DocumentUrlResponse(
    val id: Long,
    val url: String, // Presigned URL (temporary)
)