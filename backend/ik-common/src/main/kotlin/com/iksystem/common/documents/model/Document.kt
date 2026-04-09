package com.iksystem.common.documents.model

import com.iksystem.common.user.model.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

/**
 * Represents a document stored in S3 with its metadata persisted in the database.
 *
 * Each document is associated with:
 * - An organization (for multi-tenant data isolation)
 * - A user who uploaded the document
 *
 * The actual file is stored in S3, while this entity stores
 * references and metadata needed for retrieval and management.
 */
@Entity
@Table(name = "documents")
data class Document(
    /**
     * Unique identifier for the document.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * ID of the organization that owns the document.
     */
    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

    /**
     * Key used to locate the file in S3.
     */
    @Column(name = "s3_key", nullable = false)
    val s3Key: String,

    /**
     * Original name of the uploaded file.
     */
    @Column(nullable = false)
    val fileName: String,

    /**
     * MIME type of the file.
     */
    @Column(name = "content_type", nullable = false)
    val contentType: String,

    /**
     * User who uploaded the document.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    val uploadedByUser: User,

    /**
     * Timestamp when the document was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    /**
     * Timestamp of the last update to the document metadata.
     */
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
)