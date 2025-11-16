package org.kulus.android.ui.screens

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kulus.android.service.OCRResult
import org.kulus.android.service.OCRService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed class CameraUiState {
    object Ready : CameraUiState()
    object CapturingPhoto : CameraUiState()
    data class PhotoCaptured(val uri: Uri) : CameraUiState()
    data class ProcessingOCR(val uri: Uri) : CameraUiState()
    data class OCRComplete(
        val uri: Uri,
        val value: Double,
        val unit: String,
        val confidence: String,
        val rawText: String
    ) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ocrService: OCRService
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Ready)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var currentPhotoUri: Uri? = null

    /**
     * Create a temporary file URI for capturing photo
     */
    fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "GLUCOSE_${timeStamp}.jpg"
        val storageDir = File(context.cacheDir, "images").apply {
            if (!exists()) mkdirs()
        }
        val imageFile = File(storageDir, imageFileName)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        ).also {
            currentPhotoUri = it
        }
    }

    /**
     * Handle photo capture completion
     */
    fun onPhotoCaptured(uri: Uri) {
        _uiState.value = CameraUiState.PhotoCaptured(uri)
    }

    /**
     * Process captured photo with OCR
     */
    fun processPhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.ProcessingOCR(uri)

            when (val result = ocrService.extractGlucoseValue(uri)) {
                is OCRResult.Success -> {
                    _uiState.value = CameraUiState.OCRComplete(
                        uri = uri,
                        value = result.value,
                        unit = result.unit,
                        confidence = result.confidence.displayName,
                        rawText = result.rawText
                    )
                }

                is OCRResult.NoTextFound -> {
                    _uiState.value = CameraUiState.Error(
                        "No text found in the image. Please try again with a clearer photo."
                    )
                }

                is OCRResult.NoGlucoseValueFound -> {
                    _uiState.value = CameraUiState.Error(
                        "No glucose value detected. Found text: ${result.rawText.take(100)}..."
                    )
                }

                is OCRResult.Error -> {
                    _uiState.value = CameraUiState.Error(
                        "OCR failed: ${result.message}"
                    )
                }
            }
        }
    }

    /**
     * Retry photo capture
     */
    fun retry() {
        _uiState.value = CameraUiState.Ready
        currentPhotoUri = null
    }

    /**
     * Reset to initial state
     */
    fun reset() {
        _uiState.value = CameraUiState.Ready
        currentPhotoUri = null
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up temp files if needed
        currentPhotoUri?.let { uri ->
            try {
                val file = File(uri.path ?: return@let)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}
