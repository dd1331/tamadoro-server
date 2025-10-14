package com.hobos.tamadoro.domain.purchase

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InAppPurchaseRepository : JpaRepository<InAppPurchase, Long> {
    fun existsByTransactionId(transactionId: String): Boolean
}

