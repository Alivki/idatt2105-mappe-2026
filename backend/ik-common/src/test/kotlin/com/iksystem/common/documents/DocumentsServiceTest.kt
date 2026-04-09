package com.iksystem.common.documents.service

import com.iksystem.common.documents.model.Document
import com.iksystem.common.documents.repository.DocumentRepository
import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.model.User
import com.iksystem.common.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import java.net.URL
import java.time.Duration
import java.util.Optional

class DocumentsServiceTest {

    private lateinit var s3Client: S3Client
    private lateinit var s3Presigner: S3Presigner
    private lateinit var documentRepository: DocumentRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: DocumentsService

    @BeforeEach
    fun setUp() {
        s3Client = mock(S3Client::class.java)
        s3Presigner = mock(S3Presigner::class.java)
        documentRepository = mock(DocumentRepository::class.java)
        userRepository = mock(UserRepository::class.java)

        service = DocumentsService(
            s3Client = s3Client,
            s3Presigner = s3Presigner,
            documentRepository = documentRepository,
            userRepository = userRepository,
            bucketName = "test-bucket"
        )
    }

    @Test
    fun `test delegates to s3 listBuckets`() {
        service.test()
        verify(s3Client).listBuckets()
    }

    @Test
    fun `uploadFile uploads to s3 and saves metadata`() {
        val auth = auth()
        val file = mock(MultipartFile::class.java)
        val user = user()

        `when`(file.originalFilename).thenReturn("policy.pdf")
        `when`(file.contentType).thenReturn("application/pdf")
        `when`(file.bytes).thenReturn("hello".toByteArray())
        `when`(userRepository.findById(10L)).thenReturn(Optional.of(user))
        `when`(documentRepository.save(any(Document::class.java))).thenAnswer { invocation ->
            val saved = invocation.arguments[0] as Document
            saved.copy(id = 77L)
        }

        val result = service.uploadFile(file, "documents", auth)

        assertEquals(77L, result.id)
        assertEquals(1L, result.organizationId)
        assertEquals("policy.pdf", result.fileName)
        assertEquals("application/pdf", result.contentType)
        assertEquals(user.id, result.uploadedByUser.id)
        assertEquals(true, result.s3Key.startsWith("documents/"))
        assertEquals(true, result.s3Key.endsWith("-policy.pdf"))

        val putCaptor = ArgumentCaptor.forClass(PutObjectRequest::class.java)
        val bodyCaptor = ArgumentCaptor.forClass(RequestBody::class.java)
        verify(s3Client).putObject(putCaptor.capture(), bodyCaptor.capture())
        assertEquals("test-bucket", putCaptor.value.bucket())
        assertEquals("application/pdf", putCaptor.value.contentType())
        assertEquals(result.s3Key, putCaptor.value.key())

        val docCaptor = ArgumentCaptor.forClass(Document::class.java)
        verify(documentRepository).save(docCaptor.capture())
        assertEquals(1L, docCaptor.value.organizationId)
        assertEquals("policy.pdf", docCaptor.value.fileName)
        assertEquals("application/pdf", docCaptor.value.contentType)
    }

    @Test
    fun `uploadFile rejects file with no content type`() {
        val auth = auth()
        val file = mock(MultipartFile::class.java)

        `when`(file.isEmpty).thenReturn(false)
        `when`(file.size).thenReturn(3L)
        `when`(file.contentType).thenReturn(null)

        assertThrows(BadRequestException::class.java) {
            service.uploadFile(file, "docs", auth)
        }
    }

    @Test
    fun `uploadFile throws when user is missing`() {
        val auth = auth()
        val file = mock(MultipartFile::class.java)

        `when`(file.originalFilename).thenReturn("missing-user.txt")
        `when`(file.contentType).thenReturn("text/plain")
        `when`(file.bytes).thenReturn("x".toByteArray())
        `when`(userRepository.findById(10L)).thenReturn(Optional.empty())

        assertThrows(IllegalArgumentException::class.java) {
            service.uploadFile(file, "documents", auth)
        }
    }

    @Test
    fun `deleteFile deletes from s3 and repository`() {
        val document = document(id = 5L, s3Key = "documents/a.pdf")
        `when`(documentRepository.findByIdAndOrganizationId(5L, 1L)).thenReturn(document)

        service.deleteFile(5L, 1L)

        val deleteCaptor = ArgumentCaptor.forClass(DeleteObjectRequest::class.java)
        verify(s3Client).deleteObject(deleteCaptor.capture())
        assertEquals("test-bucket", deleteCaptor.value.bucket())
        assertEquals("documents/a.pdf", deleteCaptor.value.key())

        verify(documentRepository).delete(document)
    }

    @Test
    fun `deleteFile throws when document is missing`() {
        `when`(documentRepository.findByIdAndOrganizationId(404L, 1L)).thenReturn(null)

        assertThrows(IllegalArgumentException::class.java) {
            service.deleteFile(404L, 1L)
        }
    }

    @Test
    fun `getDocument returns document when found`() {
        val document = document(id = 8L)
        `when`(documentRepository.findByIdAndOrganizationId(8L, 1L)).thenReturn(document)

        val result = service.getDocument(8L, 1L)

        assertNotNull(result)
        assertEquals(8L, result!!.id)
    }

    @Test
    fun `getDocument returns null when not found`() {
        `when`(documentRepository.findByIdAndOrganizationId(8L, 1L)).thenReturn(null)

        val result = service.getDocument(8L, 1L)

        assertNull(result)
    }

    @Test
    fun `getFileUrl returns presigned url`() {
        val document = document(id = 9L, s3Key = "documents/policy.pdf")
        val presigned = mock(PresignedGetObjectRequest::class.java)
        val duration = Duration.ofMinutes(15)

        `when`(documentRepository.findByIdAndOrganizationId(9L, 1L)).thenReturn(document)
        `when`(s3Presigner.presignGetObject(any(GetObjectPresignRequest::class.java))).thenReturn(presigned)
        `when`(presigned.url()).thenReturn(URL("https://example.com/presigned"))

        val result = service.getFileUrl(9L, 1L, duration)

        assertEquals("https://example.com/presigned", result)

        val presignCaptor = ArgumentCaptor.forClass(GetObjectPresignRequest::class.java)
        verify(s3Presigner).presignGetObject(presignCaptor.capture())
        assertEquals(duration, presignCaptor.value.signatureDuration())

        val getObjectRequest: GetObjectRequest = presignCaptor.value.getObjectRequest()
        assertEquals("test-bucket", getObjectRequest.bucket())
        assertEquals("documents/policy.pdf", getObjectRequest.key())
    }

    @Test
    fun `getFileUrl throws when document is missing`() {
        `when`(documentRepository.findByIdAndOrganizationId(999L, 1L)).thenReturn(null)

        assertThrows(IllegalArgumentException::class.java) {
            service.getFileUrl(999L, 1L)
        }
    }

    private fun auth() = AuthenticatedUser(
        10L,
        1L,
        "MANAGER"
    )

    private fun user() = User(
        id = 10L,
        email = "user@example.com",
        password = "hashed",
        fullName = "Test User",
        phoneNumber = "+4712345678"
    )

    private fun document(
        id: Long = 1L,
        organizationId: Long = 1L,
        s3Key: String = "documents/file.pdf",
        fileName: String = "file.pdf",
        contentType: String = "application/pdf"
    ) = Document(
        id = id,
        organizationId = organizationId,
        s3Key = s3Key,
        fileName = fileName,
        contentType = contentType,
        uploadedByUser = user()
    )
}