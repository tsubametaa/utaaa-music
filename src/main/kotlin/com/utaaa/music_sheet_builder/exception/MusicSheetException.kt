package com.utaaa.music_sheet_builder.exception

/**
 * Base exception for all music sheet related errors
 */
sealed class MusicSheetException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    
    /**
     * Exception thrown when parsing fails
     */
    class ParseException(message: String, cause: Throwable? = null) : MusicSheetException(message, cause)
    
    /**
     * Exception thrown when export fails
     */
    class ExportException(message: String, cause: Throwable? = null) : MusicSheetException(message, cause)
    
    /**
     * Exception thrown when validation fails
     */
    class ValidationException(message: String, cause: Throwable? = null) : MusicSheetException(message, cause)
    
    /**
     * Exception thrown when storage operation fails
     */
    class StorageException(message: String, cause: Throwable? = null) : MusicSheetException(message, cause)
    
    /**
     * Exception thrown when a resource is not found
     */
    class NotFoundException(message: String) : MusicSheetException(message)
}
