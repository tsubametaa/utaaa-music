package com.utaaa.music_sheet_builder.ui.component

import com.utaaa.music_sheet_builder.model.MusicSheet
import com.utaaa.music_sheet_builder.service.AudioService
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.select.Select
import elemental.json.Json
import elemental.json.JsonArray

/**
 * Audio player component for music sheet playback
 */
class AudioPlayerComponent(
    private val audioService: AudioService
) : Div() {
    
    private var currentSheet: MusicSheet? = null
    private var currentTempo: Int = 120
    
    private val playButton = Button(VaadinIcon.PLAY.create()).apply {
        addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON)
        addClickListener { onPlayClick() }
        style.set("border-radius", "10px")
        style.set("box-shadow", "0 2px 8px rgba(124, 111, 242, 0.3)")
    }
    
    private val pauseButton = Button(VaadinIcon.PAUSE.create()).apply {
        addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON)
        addClickListener { onPauseClick() }
        isVisible = false
        style.set("border-radius", "10px")
        style.set("box-shadow", "0 2px 8px rgba(124, 111, 242, 0.3)")
    }
    
    private val stopButton = Button(VaadinIcon.STOP.create()).apply {
        addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST)
        addClickListener { onStopClick() }
        style.set("border-radius", "10px")
    }
    
    private val tempoSelect = Select<Int>().apply {
        setItems(60, 80, 100, 120, 140, 160, 180)
        value = 120
        label = "🎵 Tempo (BPM)"
        width = "140px"
        style.set("--vaadin-input-field-border-radius", "10px")
        addValueChangeListener { event ->
            currentTempo = event.value
        }
    }
    
    private val statusLabel = Span("Ready to play").apply {
        style.set("margin-left", "8px")
        style.set("color", "#6b7280")
        style.set("font-weight", "500")
        style.set("font-size", "13px")
    }
    
    init {
        addClassName("modern-card")
        style.set("padding", "16px 20px")
        style.set("background", "linear-gradient(135deg, #f0f4ff 0%, #fdf4ff 100%)")
        style.set("border-radius", "16px")
        style.set("border", "1px solid #e0eaff")
        style.set("box-shadow", "0 4px 12px rgba(124, 111, 242, 0.15)")
        
        val controls = HorizontalLayout().apply {
            isSpacing = true
            style.set("gap", "12px")
            defaultVerticalComponentAlignment = com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER
            add(playButton, pauseButton, stopButton, tempoSelect, statusLabel)
        }
        
        add(controls)
    }
    
    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        // Load the audio player script from static resources
        attachEvent.ui.page.addJavaScript("/audio-player.js")
    }
    
    override fun onDetach(detachEvent: DetachEvent) {
        super.onDetach(detachEvent)
        // Clean up audio resources
        executeJs("if (window.audioPlayer) { window.audioPlayer.dispose(); }")
    }
    
    /**
     * Load a music sheet for playback
     */
    fun loadSheet(sheet: MusicSheet) {
        currentSheet = sheet
        
        // Convert sheet to audio events
        val events = audioService.convertToAudioEvents(sheet, currentTempo)
        val duration = audioService.getTotalDuration(sheet, currentTempo)
        
        // Convert events to JSON
        val jsonEvents = Json.createArray()
        events.forEach { event ->
            val jsonEvent = Json.createObject()
            jsonEvent.put("time", event.time)
            jsonEvent.put("note", event.note)
            jsonEvent.put("duration", event.duration)
            jsonEvent.put("velocity", event.velocity)
            jsonEvent.put("hand", event.hand)
            jsonEvents.set(jsonEvents.length(), jsonEvent)
        }
        
        // Send to frontend
        executeJs(
            """
            if (!window.audioPlayer) {
                window.audioPlayer = new MusicSheetAudioPlayer();
            }
            window.audioPlayer.loadEvents($0);
            """.trimIndent(),
            jsonEvents
        )
        
        statusLabel.text = "Loaded: ${sheet.title} (${String.format("%.1f", duration)}s)"
        statusLabel.style.set("color", "#7c6ff2")
        statusLabel.style.set("font-weight", "600")
        playButton.isEnabled = true
        stopButton.isEnabled = true
    }
    
    private fun onPlayClick() {
        if (currentSheet == null) {
            statusLabel.text = "No sheet loaded"
            return
        }
        
        // Reload with current tempo if tempo changed
        currentSheet?.let { loadSheet(it) }
        
        executeJs("if (window.audioPlayer) { window.audioPlayer.play(); }")
        
        playButton.isVisible = false
        pauseButton.isVisible = true
        statusLabel.text = "Playing..."
        statusLabel.style.set("color", "#22c55e")
        statusLabel.style.set("font-weight", "600")
    }
    
    private fun onPauseClick() {
        executeJs("if (window.audioPlayer) { window.audioPlayer.pause(); }")
        
        playButton.isVisible = true
        pauseButton.isVisible = false
        statusLabel.text = "Paused"
        statusLabel.style.set("color", "#f59e0b")
        statusLabel.style.set("font-weight", "600")
    }
    
    private fun onStopClick() {
        executeJs("if (window.audioPlayer) { window.audioPlayer.stop(); }")
        
        playButton.isVisible = true
        pauseButton.isVisible = false
        statusLabel.text = "Stopped"
        statusLabel.style.set("color", "#6b7280")
        statusLabel.style.set("font-weight", "500")
    }
    
    private fun executeJs(script: String, vararg params: java.io.Serializable) {
        UI.getCurrent()?.page?.executeJs(script, *params)
    }
}
