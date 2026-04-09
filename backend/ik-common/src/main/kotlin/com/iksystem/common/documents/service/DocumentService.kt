package com.iksystem.common.documents.service

import com.iksystem.common.documents.model.Document
import com.iksystem.common.documents.repository.DocumentRepository
import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration
import java.util.UUID

@Service
class DocumentsService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val documentRepository: DocumentRepository,
    private val userRepository: UserRepository,
    @Value("\${aws.s3.bucket}") val bucketName: String,
) {
    private val log = LoggerFactory.getLogger(DocumentsService::class.java)

    fun test() {
        s3Client.listBuckets()
    }

    /**
     * Uploads a file to S3 and stores document metadata in the database.
     *
     * @param file The file to upload
     * @param folder The S3 folder path (e.g., "documents")
     * @param auth The authenticated user uploading the file
     * @return The saved Document entity with metadata
     */
    companion object {
        private const val MAX_FILE_SIZE = 5L * 1024 * 1024
        private val ALLOWED_CONTENT_TYPES = setOf(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
        )
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw BadRequestException("File is empty")
        }
        if (file.size > MAX_FILE_SIZE) {
            throw BadRequestException("File size exceeds maximum of 5 MB")
        }
        val contentType = file.contentType ?: throw BadRequestException("File has no content type")
        if (contentType !in ALLOWED_CONTENT_TYPES) {
            throw BadRequestException("File type '$contentType' is not allowed")
        }
    }

    fun uploadFile(file: MultipartFile, folder: String, auth: AuthenticatedUser): Document {
        validateFile(file)
        val s3Key = "$folder/${UUID.randomUUID()}-${file.originalFilename}"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .contentType(file.contentType)
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.bytes))
        log.info("Uploaded file to S3: key={}, size={}", s3Key, file.size)

        val user = userRepository.findById(auth.userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val document = Document(
            organizationId = auth.requireOrganizationId(),
            s3Key = s3Key,
            fileName = file.originalFilename ?: "file",
            contentType = file.contentType ?: "application/octet-stream",
            uploadedByUser = user,
        )

        return documentRepository.save(document)
    }

    /**
     * Deletes a document from S3 and removes metadata from database.
     *
     * @param documentId The document ID
     * @param organizationId The organization ID (for scope verification)
     */
    fun deleteFile(documentId: Long, organizationId: Long) {
        val document = documentRepository.findByIdAndOrganizationId(documentId, organizationId)
            ?: throw IllegalArgumentException("Document not found")

        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(document.s3Key)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
        log.info("Deleted document: id={}, s3Key={}", documentId, document.s3Key)

        documentRepository.delete(document)
    }

    /**
     * Gets a document by ID with organization scope check.
     *
     * @param documentId The document ID
     * @param organizationId The organization ID (for scope verification)
     * @return The Document or null if not found
     */
    fun getDocument(documentId: Long, organizationId: Long): Document? {
        return documentRepository.findByIdAndOrganizationId(documentId, organizationId)
    }

    /**
     * Generates a presigned URL for accessing a document in S3.
     * The URL is valid for the specified duration (default: 1 hour).
     *
     * @param documentId The document ID
     * @param organizationId The organization ID (for scope verification)
     * @param duration How long the URL is valid (default 1 hour)
     * @return A presigned URL that can be used directly in the browser
     */
    fun uploadBytes(
        bytes: ByteArray,
        fileName: String,
        contentType: String,
        folder: String,
        auth: AuthenticatedUser,
    ): Document {
        val s3Key = "$folder/${UUID.randomUUID()}-$fileName"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .contentType(contentType)
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes))

        val user = userRepository.findById(auth.userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val document = Document(
            organizationId = auth.requireOrganizationId(),
            s3Key = s3Key,
            fileName = fileName,
            contentType = contentType,
            uploadedByUser = user,
        )

        return documentRepository.save(document)
    }

    fun getFileUrl(documentId: Long, organizationId: Long, duration: Duration = Duration.ofHours(1)): String {
        val document = getDocument(documentId, organizationId)
            ?: throw IllegalArgumentException("Document not found")

        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(document.s3Key)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(duration)
            .getObjectRequest(getObjectRequest)
            .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }
}