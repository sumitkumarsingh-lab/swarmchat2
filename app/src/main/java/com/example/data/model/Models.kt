package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Customer lookup request parameters.
 */
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "search_param") val searchParam: String
)

/**
 * Enterprise representative user model structure.
 */
@JsonClass(generateAdapter = true)
data class UserData(
    @Json(name = "account_number") val accountNumber: String,
    @Json(name = "name") val name: String,
    @Json(name = "tier") val tier: String,
    @Json(name = "status") val status: String
)

/**
 * Response payload mapping from mobile login checks.
 */
@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "authenticated") val authenticated: Boolean,
    @Json(name = "user_data") val userData: UserData
)

/**
 * Registration request payload mapping to app.py model.
 */
@JsonClass(generateAdapter = true)
data class RegistrationRequest(
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "email") val email: String,
    @Json(name = "plan_tier") val planTier: String
)

/**
 * Response payload mapping from registration endpoint.
 */
@JsonClass(generateAdapter = true)
data class RegistrationResponse(
    @Json(name = "authenticated") val authenticated: Boolean,
    @Json(name = "user_data") val userData: UserData
)

/**
 * Interactive support ticket message.
 */
@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role") val role: String, // "user" or "assistant"
    @Json(name = "content") val content: String
)

/**
 * Dynamic chat payload matching FastAPI endpoint expectations.
 */
@JsonClass(generateAdapter = true)
data class ChatRequest(
    @Json(name = "messages") val messages: List<ChatMessage>,
    @Json(name = "user_name") val userName: String
)

/**
 * Response returned from the Swarm triage supervisor client mapping.
 */
@JsonClass(generateAdapter = true)
data class ChatResponse(
    @Json(name = "response_text") val responseText: String,
    @Json(name = "handled_by") val handledBy: String
)
