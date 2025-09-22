package com.hobos.tamadoro.api.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun success(message: String? = null): ApiResponse<Unit> =
            ApiResponse(success = true, data = null, message = message, error = null)

        fun <T> success(data: T, message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message, error = null)

        fun <T> successNullable(data: T? = null, message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message, error = null)

        fun <T> error(code: Int, message: String, details: Any? = null): ApiResponse<T> =
            ApiResponse(success = false, data = null, message = null, error = ErrorResponse(code, message, details))
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
