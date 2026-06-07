package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.data.Spark
import com.example.data.SparkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val sparkText: String) : UiState
    data class Error(val message: String) : UiState
}

class SparkViewModel(private val repository: SparkRepository) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedVibe = MutableStateFlow("Zen Mindfulness")
    val selectedVibe: StateFlow<String> = _selectedVibe.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val savedSparks: StateFlow<List<Spark>> = repository.allItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isApiKeyConfigured: Boolean
        get() {
            val key = BuildConfig.GEMINI_API_KEY
            return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && key != "GEMINI_API_KEY"
        }

    fun onInputTextChanged(newValue: String) {
        _inputText.value = newValue
    }

    fun onVibeSelected(vibe: String) {
        _selectedVibe.value = vibe
    }

    fun generateSpark() {
        val vibe = _selectedVibe.value
        val userInput = _inputText.value.trim()

        if (!isApiKeyConfigured) {
            _uiState.value = UiState.Error("API Key is missing or default. Please add GEMINI_API_KEY in the Secrets panel in AI Studio.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val prompt = buildPrompt(vibe, userInput)
            val request = com.example.api.GenerateContentRequest(
                contents = listOf(
                    com.example.api.Content(
                        parts = listOf(com.example.api.Part(text = prompt))
                    )
                )
            )

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val textResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (textResult != null && textResult.isNotBlank()) {
                    _uiState.value = UiState.Success(textResult)

                    // Auto-save the generated spark to our Room database
                    val spark = Spark(
                        prompt = if (userInput.isNotEmpty()) userInput else "A fresh daily spark",
                        response = textResult,
                        vibe = vibe,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insert(spark)
                } else {
                    _uiState.value = UiState.Error("Failed to generate inspiration. The cosmic wind returned empty.")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred in the cosmos.")
            }
        }
    }

    fun deleteSpark(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun toggleFavorite(spark: Spark) {
        viewModelScope.launch {
            repository.setFavorite(spark.id, !spark.isFavorite)
        }
    }

    private fun buildPrompt(vibe: String, customInput: String): String {
        val baseTheme = when (vibe) {
            "Zen Mindfulness" -> "a serene, tranquil spark of mindfulness, a brief meditation cue, or a poetic cosmic reflection. Focus on deep breathing, grounding, and high-frequency inner peace."
            "Cosmic Adventure" -> "an epic, inspiring sci-fi quote, stargazing observation, or galactic adventure starter that evokes awe and boundless curiosity about the stars."
            "Productivity Spark" -> "a sharp, energetic motivational catalyst, focus strategy, or structured daily challenge designed to break procrastination and ignite focus."
            "Creative Writer" -> "a unique creative writing prompt, world building idea, or a striking lyrical sentence that starts a story of incredible wonder."
            "Idea Starter" -> "a disruptive digital concept, project hypothesis, or design seed designed to spark entrepreneurial or hardware innovation."
            else -> "a generic uplifting prompt of positive expansion."
        }

        val details = if (customInput.isNotEmpty()) {
            "Specifically incorporate: \"$customInput\"."
        } else {
            "Create it freely but make sure it is fully self-contained, inspiring, and elegant."
        }

        return """
            You are Cosmic Spark, a majestic AI muse.
            Generate a short, beautifully formatted response providing $baseTheme
            $details
            
            Format guidelines:
            - Provide a stunning title bounded by emoji (e.g. "🌌 cosmic blueprint 🌌").
            - Write 2-3 short, highly artistic paragraphs or brief poetic bullet points.
            - Ensure it is evocative, memorable, elegant, and deeply inspirational.
            - Keep it under 150 words. Do not use markdown backticks, markdown lists, or heavy bold lines. Just beautifully clean paragraph breaks and subtle cosmic emojis.
        """.trimIndent()
    }
}

class SparkViewModelFactory(private val repository: SparkRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SparkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SparkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
