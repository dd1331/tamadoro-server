package com.hobos.tamadoro.domain.tamas

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

// TODO: 카테고리별로 테이블 구성
@Entity
@Table(
    name = "tamas",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id",  "item_id"])],
)
class UserTama(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tama_id", nullable = false)
    val tama: TamaCatalogEntity,


    @Column(name = " name", nullable = true)
    var name: String = "",

    @Column(name = "is_active")
    var isActive: Boolean = false,
) {



    @Column(name = "experience")
    var experience: Int = 0
        protected set


    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()


    fun addExperience(amount: Int) {
        require(amount > 0)
        this.experience += amount
    }






}
