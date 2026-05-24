package com.utaaa.music_sheet_builder

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.theme.Theme
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for Music Sheet Builder
 */
@SpringBootApplication
@Theme("my-theme")
class MusicSheetBuilderApplication : AppShellConfigurator {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {}
    logger.info { "Starting Music Sheet Builder Application..." }
    runApplication<MusicSheetBuilderApplication>(*args)
}
