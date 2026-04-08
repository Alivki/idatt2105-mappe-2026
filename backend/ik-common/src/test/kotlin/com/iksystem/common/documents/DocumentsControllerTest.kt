package com.iksystem.common.documents.controller

import com.iksystem.common.documents.dto.DocumentUploadResponse
import com.iksystem.common.documents.model.Document
import com.iksystem.common.documents.service.DocumentsService
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.web.multipart.MultipartFile

class DocumentsControllerTest {

    private lateinit var documentsService: DocumentsService
    private lateinit var controller: DocumentsController

    @BeforeEach
    fun setUp() {
        documentsService = mock(DocumentsService::class.java)
        controller = DocumentsController(documentsService)
    }

    @Test
    fun `test returns OK`() {
        val result = controller.test()

        assertEquals("OK", result)
        verify(documentsService).test()
    }

    @Test
    fun `uploadDocument returns 201 with upload response`() {
        val file = mock(MultipartFile::class.java)
        val auth = auth()
        val document = document(id = 11L, s3Key = "documents/abc-file.pdf", fileName = "file.pdf")

        `when`(documentsService.uploadFile(file, "documents", auth)).thenReturn(document)
        `when`(documentsService.getFileUrl(11L, 1L)).thenReturn("https://example.com/file.pdf")

        val result = controller.uploadDocument(file, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(11L, result.body!!.id)
        assertEquals("file.pdf", result.body!!.fileName)
        assertEquals("documents/abc-file.pdf", result.body!!.s3Key)
        assertEquals("application/pdf", result.body!!.contentType)
        assertEquals("https://example.com/file.pdf", result.body!!.url)

        verify(documentsService).uploadFile(file, "documents", auth)
        verify(documentsService).getFileUrl(11L, 1L)
    }

    @Test
    fun `getDocumentUrl returns 200 with url response`() {
        val auth = auth()

        `when`(documentsService.getFileUrl(22L, 1L)).thenReturn("https://example.com/presigned")

        val result = controller.getDocumentUrl(22L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(22L, result.body!!.id)
        assertEquals("https://example.com/presigned", result.body!!.url)

        verify(documentsService).getFileUrl(22L, 1L)
    }

    @Test
    fun `deleteDocument returns 204 and empty body`() {
        val auth = auth()

        val result = controller.deleteDocument(33L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(documentsService).deleteFile(33L, 1L)
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
        id: Long,
        s3Key: String,
        fileName: String,
        contentType: String = "application/pdf"
    ) = Document(
        id = id,
        organizationId = 1L,
        s3Key = s3Key,
        fileName = fileName,
        contentType = contentType,
        uploadedByUser = user()
    )
}