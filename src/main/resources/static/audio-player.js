/**
 * Audio Player using Web Audio API for music sheet playback
 */

class MusicSheetAudioPlayer {
  constructor() {
    this.audioContext = null;
    this.scheduledNotes = [];
    this.isPlaying = false;
    this.startTime = 0;
    this.pauseTime = 0;
    this.events = [];
  }

  /**
   * Initialize audio context (must be called after user interaction)
   */
  initAudioContext() {
    if (!this.audioContext) {
      this.audioContext = new (
        window.AudioContext || window.webkitAudioContext
      )();
    }
  }

  /**
   * Load audio events for playback
   */
  loadEvents(events) {
    this.events = events;
  }

  /**
   * Convert note name to frequency in Hz
   */
  noteToFrequency(note) {
    const noteMap = {
      C: 0,
      "C#": 1,
      Db: 1,
      D: 2,
      "D#": 3,
      Eb: 3,
      E: 4,
      F: 5,
      "F#": 6,
      Gb: 6,
      G: 7,
      "G#": 8,
      Ab: 8,
      A: 9,
      "A#": 10,
      Bb: 10,
      B: 11,
    };

    // Parse note (e.g., "C4", "F#5", "Bb3")
    const match = note.match(/([A-G][#b]?)(\d?)/);
    if (!match) return 440; // Default to A4

    const noteName = match[1];
    const octave = parseInt(match[2] || "4");

    const noteValue = noteMap[noteName] || 0;
    const midiNote = (octave + 1) * 12 + noteValue;

    // Convert MIDI note to frequency: f = 440 * 2^((n-69)/12)
    return 440 * Math.pow(2, (midiNote - 69) / 12);
  }

  /**
   * Create a piano-like sound using oscillators
   */
  playNote(frequency, startTime, duration, velocity) {
    if (!this.audioContext) return;

    const now = this.audioContext.currentTime;
    const actualStartTime = now + startTime;
    const actualEndTime = actualStartTime + duration;

    // Create oscillator for fundamental frequency
    const oscillator = this.audioContext.createOscillator();
    const gainNode = this.audioContext.createGain();

    // Use triangle wave for a softer piano-like sound
    oscillator.type = "triangle";
    oscillator.frequency.setValueAtTime(frequency, actualStartTime);

    // ADSR envelope for piano-like sound
    const attackTime = 0.01;
    const decayTime = 0.1;
    const sustainLevel = velocity * 0.6;
    const releaseTime = 0.2;

    // Attack
    gainNode.gain.setValueAtTime(0, actualStartTime);
    gainNode.gain.linearRampToValueAtTime(
      velocity,
      actualStartTime + attackTime,
    );

    // Decay
    gainNode.gain.linearRampToValueAtTime(
      sustainLevel,
      actualStartTime + attackTime + decayTime,
    );

    // Sustain (hold at sustainLevel)
    gainNode.gain.setValueAtTime(sustainLevel, actualEndTime - releaseTime);

    // Release
    gainNode.gain.linearRampToValueAtTime(0, actualEndTime);

    // Connect nodes
    oscillator.connect(gainNode);
    gainNode.connect(this.audioContext.destination);

    // Schedule start and stop
    oscillator.start(actualStartTime);
    oscillator.stop(actualEndTime);

    // Store reference for cleanup
    this.scheduledNotes.push(oscillator);
  }

  /**
   * Play the loaded music sheet
   */
  play() {
    this.initAudioContext();

    if (!this.audioContext || this.events.length === 0) {
      console.error("No audio context or events loaded");
      return;
    }

    if (this.isPlaying) {
      return; // Already playing
    }

    this.isPlaying = true;
    this.startTime = this.audioContext.currentTime - this.pauseTime;

    // Schedule all notes
    this.events.forEach((event) => {
      const frequency = this.noteToFrequency(event.note);
      const startTime = event.time - this.pauseTime;
      this.playNote(frequency, startTime, event.duration, event.velocity);
    });

    // Auto-stop when finished
    const totalDuration = Math.max(
      ...this.events.map((e) => e.time + e.duration),
    );
    setTimeout(
      () => {
        if (this.isPlaying) {
          this.stop();
        }
      },
      (totalDuration - this.pauseTime) * 1000,
    );
  }

  /**
   * Pause playback
   */
  pause() {
    if (!this.audioContext || !this.isPlaying) return;

    this.pauseTime = this.audioContext.currentTime - this.startTime;
    this.stopAllNotes();
    this.isPlaying = false;
  }

  /**
   * Stop playback and reset
   */
  stop() {
    this.stopAllNotes();
    this.pauseTime = 0;
    this.isPlaying = false;
  }

  /**
   * Stop all currently playing notes
   */
  stopAllNotes() {
    this.scheduledNotes.forEach((note) => {
      try {
        note.stop();
      } catch (e) {
        // Note might already be stopped
      }
    });
    this.scheduledNotes = [];
  }

  /**
   * Check if currently playing
   */
  getIsPlaying() {
    return this.isPlaying;
  }

  /**
   * Clean up resources
   */
  dispose() {
    this.stop();
    if (this.audioContext) {
      this.audioContext.close();
      this.audioContext = null;
    }
  }
}

// Export for use in Vaadin components
window.MusicSheetAudioPlayer = MusicSheetAudioPlayer;
