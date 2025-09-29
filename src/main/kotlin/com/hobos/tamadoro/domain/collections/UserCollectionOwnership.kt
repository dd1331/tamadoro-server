package com.hobos.tamadoro.domain.collections

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

// TODO: 카테고리별로 테이블 구성
@Entity
@Table(
    name = "user_collection_ownerships",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "category", "item_id"])],
    indexes = [Index(name = "idx_user_category", columnList = "user_id,category")]
)
class UserCollectionOwnership(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    val category: CollectionCategory,

    @Column(name = "item_id", nullable = false)
    val itemId: Long,

    @Column(name = "purchased_at", nullable = false)
    val purchasedAt: LocalDateTime = LocalDateTime.now(),
)
