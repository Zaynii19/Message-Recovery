package com.example.messagerecovery.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

data class AudioPlaybackState(
    val currentAttachmentId: Long? = null,
    val filePath: String? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0
)

@Singleton
class AudioPlayerManager @Inject constructor() {

    companion object {
        private const val TAG = "AudioPlayerManager"
    }

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    @Synchronized
    fun playAudio(attachmentId: Long, filePath: String) {
        val currentState = _playbackState.value

        // If the same file is currently paused, simply resume
        if (currentState.currentAttachmentId == attachmentId && !currentState.isPlaying && mediaPlayer != null) {
            resumeAudio()
            return
        }

        // If another audio is playing, stop it first
        stopAudio()

        val file = File(filePath)
        if (!file.exists()) return

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(filePath)
                prepare()
                start()
            }
            mediaPlayer = player

            val duration = player.duration.coerceAtLeast(0)
            _playbackState.value = AudioPlaybackState(
                currentAttachmentId = attachmentId,
                filePath = filePath,
                isPlaying = true,
                currentPositionMs = 0,
                durationMs = duration
            )

            player.setOnCompletionListener {
                Log.d(TAG, "Audio playback completed: $filePath")
                stopAudio()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer encountered error: what=$what, extra=$extra for file $filePath")
                stopAudio()
                true
            }

            Log.d(TAG, "Started audio playback: $filePath (duration=$duration ms)")
            startProgressTracking()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio playback for $filePath", e)
            stopAudio()
        }
    }

    @Synchronized
    fun pauseAudio() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    Log.d(TAG, "Paused audio playback at ${player.currentPosition} ms")
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        currentPositionMs = player.currentPosition
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio playback", e)
        }
        stopProgressTracking()
    }

    @Synchronized
    fun resumeAudio() {
        try {
            mediaPlayer?.let { player ->
                player.start()
                Log.d(TAG, "Resumed audio playback at ${player.currentPosition} ms")
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startProgressTracking()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming audio playback", e)
            stopAudio()
        }
    }

    @Synchronized
    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.let { player ->
                Log.d(TAG, "Seeking audio playback to $positionMs ms")
                player.seekTo(positionMs)
                _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking audio playback", e)
        }
    }

    @Synchronized
    fun stopAudio() {
        stopProgressTracking()
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
                Log.d(TAG, "Stopped audio playback and released MediaPlayer")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio playback", e)
        } finally {
            mediaPlayer = null
            _playbackState.value = AudioPlaybackState()
        }
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = scope.launch {
            while (isActive) {
                delay(100.milliseconds)
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            _playbackState.value = _playbackState.value.copy(
                                currentPositionMs = player.currentPosition,
                                durationMs = player.duration.coerceAtLeast(0)
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating audio playback progress", e)
                    }
                }
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }
}
