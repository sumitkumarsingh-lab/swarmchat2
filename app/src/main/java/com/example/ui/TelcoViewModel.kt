package com.example.ui

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiConfig
import com.example.data.api.RetrofitClient
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRequest
import com.example.data.model.LoginRequest
import com.example.data.model.RegistrationRequest
import com.example.data.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Screen navigation keys representing native sub-structures.
 */
enum class AppScreen {
    LOGIN,
    REGISTRATION,
    DASHBOARD
}

/**
 * Enterprise state container managing active multiswarm conversations and health metrics.
 */
class TelcoViewModel : ViewModel() {

    // Dynamic base URL link state
    private val _baseUrlState = MutableStateFlow(ApiConfig.baseUrl)
    val baseUrlState: StateFlow<String> = _baseUrlState.asStateFlow()

    // Screen State
    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Active User session data
    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser.asStateFlow()

    // Input flow bindings
    val loginSearchParam = MutableStateFlow("")
    
    val regName = MutableStateFlow("")
    val regPhone = MutableStateFlow("")
    val regEmail = MutableStateFlow("")
    val regPlanTier = MutableStateFlow("Platinum") // Default tier selection

    val chatInputText = MutableStateFlow("")

    // Conversation thread
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Swarm Agent orchestration labels
    private val _activeAgent = MutableStateFlow("Standby")
    val activeAgent: StateFlow<String> = _activeAgent.asStateFlow()

    // Operational and connectivity metrics
    private val _isHealthy = MutableStateFlow(false)
    val isHealthy: StateFlow<Boolean> = _isHealthy.asStateFlow()

    private val _latencyText = MutableStateFlow("N/A")
    val latencyText: StateFlow<String> = _latencyText.asStateFlow()

    private val _operationsCount = MutableStateFlow(0)
    val operationsCount: StateFlow<Int> = _operationsCount.asStateFlow()

    // Interactive operations loading / error boundaries
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Run initial health audit
        pingServer()
    }

    /**
     * Updates the base target URL inside the dynamic network interceptor.
     */
    fun updateBaseUrl(newUrl: String) {
        _baseUrlState.value = newUrl
        ApiConfig.baseUrl = newUrl
        pingServer()
    }

    /**
     * Resets any active exception banner.
     */
    fun clearError() = _errorMessage.tryEmit(null)

    /**
     * Tests and pings server status to evaluate gateway health and operational delay metrics.
     */
    fun pingServer() {
        viewModelScope.launch {
            val startTime = SystemClock.elapsedRealtime()
            try {
                val resp = RetrofitClient.apiService.healthCheck()
                val latency = SystemClock.elapsedRealtime() - startTime
                if (resp["status"] == "healthy") {
                    _isHealthy.value = true
                    _latencyText.value = "${latency}ms"
                    clearError()
                } else {
                    _isHealthy.value = false
                    _latencyText.value = "Slow Link"
                }
            } catch (e: Exception) {
                _isHealthy.value = false
                _latencyText.value = "Offline"
            }
        }
    }

    /**
     * Validates and signs in an existing user using account numbers or phone lookups.
     */
    fun loginUser() {
        if (loginSearchParam.value.isBlank()) {
            _errorMessage.value = "Account parameter required"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(searchParam = loginSearchParam.value.trim())
                )
                if (response.authenticated) {
                    _currentUser.value = response.userData
                    _operationsCount.value += 1
                    _currentScreen.value = AppScreen.DASHBOARD
                    
                    // Welcome announcement from the concierge
                    val welcomeMsg = ChatMessage(
                        role = "assistant",
                        content = "Welcome back, ${response.userData.name}! I am the Triage Supervisor concierge. How can I assist you with your Global Telco account or mobile network services today?"
                    )
                    _chatMessages.value = listOf(welcomeMsg)
                    _activeAgent.value = "Triage Supervisor"
                    pingServer()
                } else {
                    _errorMessage.value = "Authentication rejected"
                }
            } catch (e: Exception) {
                val formattedErr = formatNetworkErrorMessage(e)
                _errorMessage.value = formattedErr
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Submits brand new enterprise customer profiles to the server, jumping directly into active session.
     */
    fun registerUser() {
        val nameVal = regName.value.trim()
        val phoneVal = regPhone.value.trim()
        val emailVal = regEmail.value.trim()
        val tierVal = regPlanTier.value

        if (nameVal.isEmpty() || phoneVal.isEmpty() || emailVal.isEmpty()) {
            _errorMessage.value = "Please fill in all registration fields"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.apiService.register(
                    RegistrationRequest(
                        name = nameVal,
                        phone = phoneVal,
                        email = emailVal,
                        planTier = tierVal
                    )
                )
                if (response.authenticated) {
                    _currentUser.value = response.userData
                    _operationsCount.value += 1
                    _currentScreen.value = AppScreen.DASHBOARD

                    val welcomeMsg = ChatMessage(
                        role = "assistant",
                        content = "Excellent choice, ${response.userData.name}! Your account ($nameVal) is now registered on our high-speed $tierVal tier. How can I get you started today?"
                    )
                    _chatMessages.value = listOf(welcomeMsg)
                    _activeAgent.value = "Triage Supervisor"
                    pingServer()
                } else {
                    _errorMessage.value = "Registration declined by server"
                }
            } catch (e: Exception) {
                _errorMessage.value = formatNetworkErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Conducts Swarm conversational loops. Transfers between Sales, Technical, or Loyalty.
     */
    fun sendChatMessage() {
        val messageText = chatInputText.value.trim()
        if (messageText.isEmpty()) return

        val user = _currentUser.value ?: return
        val currentHistory = _chatMessages.value.toMutableList()
        
        // Post user bubble immediately
        val userItem = ChatMessage(role = "user", content = messageText)
        currentHistory.add(userItem)
        _chatMessages.value = currentHistory
        chatInputText.value = ""

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val apiRequest = ChatRequest(
                    messages = currentHistory,
                    userName = user.name
                )
                val response = RetrofitClient.apiService.chat(apiRequest)
                _operationsCount.value += 1

                val serverReply = ChatMessage(
                    role = "assistant",
                    content = response.responseText
                )
                
                // Add server reply
                val updatedMessageStream = _chatMessages.value.toMutableList()
                updatedMessageStream.add(serverReply)
                _chatMessages.value = updatedMessageStream
                
                // Track active agent
                _activeAgent.value = response.handledBy
                pingServer()
            } catch (e: Exception) {
                _errorMessage.value = formatNetworkErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Navigates explicitly to target screens.
     */
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        _errorMessage.value = null
    }

    /**
     * Discards active cache and returns representative to Login portal.
     */
    fun logout() {
        _currentUser.value = null
        _chatMessages.value = emptyList()
        _activeAgent.value = "Standby"
        loginSearchParam.value = ""
        regName.value = ""
        regPhone.value = ""
        regEmail.value = ""
        _currentScreen.value = AppScreen.LOGIN
        _errorMessage.value = null
        pingServer()
    }

    /**
     * Translates standard exceptions into actionable developers guidance directives.
     */
    private fun formatNetworkErrorMessage(e: Exception): String {
        e.printStackTrace()
        val msg = e.localizedMessage ?: e.message ?: "Unknown socket error"
        return if (msg.contains("ConnectException") || msg.contains("Failed to connect") || msg.contains("timeout")) {
            "Gateway Unreachable.\n1. Verify your local FastAPI backend is active on port 8000.\n2. Tap 'Configuration Options' below and enter your exact local workspace container or development IP (such as wireless LAN IPv4)."
        } else {
            "API Failure: $msg"
        }
    }
}
