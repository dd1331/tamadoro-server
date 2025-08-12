package com.hobos.tamadoro.api.exception

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.api.common.ErrorResponse
import com.hobos.tamadoro.api.common.ValidationError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.util.NoSuchElementException

/**
 * Global exception handler for the application.
 * Handles exceptions and returns appropriate error responses.
 */
@ControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * Handles NoSuchElementException.
     * This is thrown when an entity is not found.
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Resource not found: ${ex.message}")
        
        val errorResponse = ApiResponse.error<Nothing>(
            code = 404,
            message = ex.message ?: "Resource not found",
            details = request.getDescription(false)
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    /**
     * Handles IllegalArgumentException.
     * This is thrown when an invalid argument is provided.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Invalid argument: ${ex.message}")
        
        val errorResponse = ApiResponse.error<Nothing>(
            code = 400,
            message = ex.message ?: "Invalid argument",
            details = request.getDescription(false)
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.allErrors.mapNotNull { err ->
            if (err is FieldError) ValidationError(err.field, err.defaultMessage ?: "Invalid value") else null
        }
        val errorResponse = ApiResponse.error<Nothing>(
            code = 400,
            message = "Validation failed",
            details = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    /**
     * Handles IllegalStateException.
     * This is thrown when the application is in an illegal state.
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Illegal state: ${ex.message}")
        
        val errorResponse = ApiResponse.error<Nothing>(
            code = 400,
            message = ex.message ?: "Illegal state",
            details = request.getDescription(false)
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    /**
     * Handles all other exceptions.
     */
    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unhandled exception", ex)
        
        val errorResponse = ApiResponse.error<Nothing>(
            code = 500,
            message = "An unexpected error occurred",
            details = request.getDescription(false)
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}