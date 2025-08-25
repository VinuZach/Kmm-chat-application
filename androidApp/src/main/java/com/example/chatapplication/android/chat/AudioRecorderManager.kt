package com.example.chatapplication.android.chat

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.IOException
import java.io.File

class AudioRecorderManager {
    private var audioPlayer = lazy { AudioPlayer() }
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private val TAG = "AudioRecorderManager12"
    fun startRecording(context: Context, outputFilePath: String) {
        outputFile = File(outputFilePath)
        try {


            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(outputFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                prepare()
                start()
            }
        } catch (e: IOException) {
            Log.d(TAG, "startRecording: $e")
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    fun getRecordedFilePath(): File? = outputFile

    fun playAudioFile(isAudioCompleted: () -> Unit): Unit {
        if (audioPlayer.value.currentAudioPosition.value==0f) {
            getRecordedFilePath()?.let { file ->
                audioPlayer.value.playAudioFromPath(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    isAudioCompleted = isAudioCompleted
                )

            }
        }
        else
            audioPlayer.value.startPlayback()

    }

    fun pauseAudioFile(): Unit {
        audioPlayer.value.pausePlayback()
    }

    fun getCurrentAudioPosition() = audioPlayer.value.currentAudioPosition

    fun getTotalAudioDuration() = audioPlayer.value.audioDuration

    fun audioScrollToPosition(position: Int) = audioPlayer.value.seekTo(position)
    class AudioPlayer {
        private var mediaPlayer: MediaPlayer? = null

        var isAudioPlaying = mutableStateOf(false)
            private set

        var isPaused = mutableStateOf(false)
            private set

        var currentAudioPosition = mutableStateOf(0f)
            private set

        var audioDuration = mutableStateOf(0)
            private set


        var currentFileName = mutableStateOf("")
            private set


        fun playAudioFromPath(
            filePath: String,
            fileName: String = "",
            isAudioCompleted: () -> Unit
        ) {
            try {
                stopPlayback() // Stop any current playback

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(filePath)
                    prepareAsync()

                    setOnPreparedListener { player ->
                        audioDuration.value = player.duration
                        currentFileName.value = fileName.ifEmpty { File(filePath).name }

                        startPlayback()
                    }

                    setOnCompletionListener {
                        isAudioPlaying.value = false
                        isPaused.value = false
                        currentAudioPosition.value = 0f
                        isAudioCompleted.invoke()

                    }

                    setOnErrorListener { _, what, extra ->
                        isAudioPlaying.value = false
                        isPaused.value = false

                        true
                    }
                }


            } catch (e: IOException) {

            }
        }

        private var positionUpdateJob: Job? = null

        fun startPlayback() {

            mediaPlayer?.let { player ->

                positionUpdateJob = CoroutineScope(Dispatchers.Main).launch {
                    while (isAudioPlaying.value) {
                        currentAudioPosition.value = player.currentPosition.toFloat()
                        delay(100)
                    }
                }
                if (isPaused.value) {
                    // Resume from paused state
                    player.start()
                    isAudioPlaying.value = true
                    isPaused.value = false

                } else {
                    // Start fresh playback
                    player.start()
                    isAudioPlaying.value = true

                }
            }
        }

        fun pausePlayback() {
            mediaPlayer?.let { player ->
                if (isAudioPlaying.value) {
                    player.pause()
                    currentAudioPosition.value = player.currentPosition.toFloat()
                    isAudioPlaying.value = false
                    isPaused.value = true
                }
            }
        }

        fun stopPlayback() {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            isAudioPlaying.value = false
            isPaused.value = false
            currentAudioPosition.value = 0f

        }

        fun seekTo(position: Int) {
            mediaPlayer?.let { player ->
                player.seekTo(position)
                currentAudioPosition.value = position.toFloat()

            }
        }

        fun getCurrentPosition(): Int {
            return mediaPlayer?.currentPosition ?: 0
        }

        fun getDuration(): Int {
            return mediaPlayer?.duration ?: 0
        }

        private fun formatTime(milliseconds: Int): String {
            val seconds = (milliseconds / 1000) % 60
            val minutes = (milliseconds / (1000 * 60)) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        fun getFormattedCurrentTime(): String = formatTime(getCurrentPosition())
        fun getFormattedDuration(): String = formatTime(getDuration())

    }


}