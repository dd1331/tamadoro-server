package com.hobos.tamadoro.api.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, data, null)
        fun <T> error(code: Int, message: String, details: Any? = null): ApiResponse<T> =
            ApiResponse(false, null, ErrorResponse(code, message, details))
    }
}

data class ErrorResponse(
    val code: Int,
    val message: String,
    val details: Any? = null
)

data class ValidationError(
    val field: String,
    val message: String
)
