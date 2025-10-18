package com.hobos.tamadoro.application.tama

data class PagingRequest(
    val page: Int = 0,
    val size: Int = 20
) {
    init {
        require(page >= 0) { "Page must be non-negative" }
        require(size > 0) { "Size must be positive" }
        require(size <= 100) { "Size must not exceed 100" }
    }
}

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
