package com.utaaa.music_sheet_builder.model

/**
 * Result of a validation operation
 */
sealed class ValidationResult {
    /**
     * Validation succeeded
     */
    data object Success : ValidationResult()
    
    /**
     * Validation failed with errors
     * @property errors List of error messages
     */
    data class Error(val errors: List<String>) : ValidationResult() {
        /**
         * Get all errors as a single formatted string
         */
        fun getErrorMessage(): String = errors.joinToString("\n• ", prefix = "Validation errors:\n• ")
    }
    
    /**
     * Check if validation was successful
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Check if validation failed
     */
    fun isError(): Boolean = this is Error
}
