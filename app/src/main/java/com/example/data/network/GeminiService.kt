package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- Network Models ---

data class AIMissionResponse(
    val title: String,
    val refinedOutcome: String,
    val successCriteria: List<String>,
    val subtasks: List<String>,
    val estimatedMinutes: Int,
    val difficulty: String, // Easy, Medium, Hard, Epic
    val category: String, // Development, Learning, Career, Business, Fitness, Writing
    val predictedBlockers: String,
    val recommendedResources: String
)

data class AIMissionListResponse(
    val missions: List<AIMissionResponse>
)

data class AIDailyBriefResponse(
    val primaryMission: String,
    val secondaryMissions: List<String>,
    val totalEstimatedEffort: String,
    val recommendedFocusOrder: String,
    val predictedBlockers: String,
    val briefingText: String
)

data class AICoachAdviceResponse(
    val advice: String,
    val suggestedSubtasks: List<String>,
    val recoveryTip: String
)

data class AIDebriefResponse(
    val summary: String,
    val lessonsLearned: String,
    val suggestedAdjustments: String
)

data class AIInsightsResponse(
    val insights: List<String>
)

class GeminiService {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key == "MY_GEMINI_API_KEY" || key.isEmpty()) {
            ""
        } else {
            key
        }
    }

    private fun cleanMarkdownJson(rawText: String): String {
        var text = rawText.trim()
        if (text.startsWith("```json")) {
            text = text.substring(7)
        } else if (text.startsWith("```")) {
            text = text.substring(3)
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length - 3)
        }
        return text.trim()
    }

    private suspend fun callGeminiApi(systemInstruction: String, prompt: String, jsonResponse: Boolean): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            throw IOException("Gemini API key is missing. Please configure it in the Secrets panel in Google AI Studio.")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestObj = JSONObject()
        
        // Contents
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", prompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        requestObj.put("contents", contentsArray)

        // System Instruction
        if (systemInstruction.isNotEmpty()) {
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemInstruction)
            sysPartsArray.put(sysPartObj)
            sysInstructionObj.put("parts", sysPartsArray)
            requestObj.put("systemInstruction", sysInstructionObj)
        }

        // Configuration (JSON mode if specified)
        if (jsonResponse) {
            val generationConfig = JSONObject()
            val responseFormat = JSONObject()
            responseFormat.put("type", "OBJECT") // Standard response setting
            responseFormat.put("mimeType", "application/json")
            generationConfig.put("responseFormat", responseFormat)
            generationConfig.put("temperature", 0.4) // Slightly lower temperature for deterministic structures
            requestObj.put("generationConfig", generationConfig)
        }

        val jsonBody = requestObj.toString()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e("GeminiService", "API error: ${response.code} $errBody")
                    throw IOException("Gemini API request failed with code ${response.code}: $errBody")
                }
                val bodyString = response.body?.string() ?: throw IOException("Empty response body")
                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    throw IOException("No generation candidates returned from Gemini.")
                }
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content") ?: throw IOException("Missing content in candidate")
                val parts = content.optJSONArray("parts") ?: throw IOException("Missing parts in content")
                if (parts.length() == 0) {
                    throw IOException("Empty parts in content")
                }
                val resultText = parts.getJSONObject(0).optString("text") ?: ""
                Log.d("GeminiService", "Raw API Response: $resultText")
                cleanMarkdownJson(resultText)
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Exception during Gemini call", e)
            throw e
        }
    }

    // --- High Level API Methods ---

    /**
     * Converts a raw goal description entered during Night Planning into a refined list of Missions with Subtasks.
     */
    suspend fun enhanceGoals(goalsText: String, historicalContext: String): AIMissionListResponse {
        val systemMessage = """
            You are the "Operator" core system, an execution coach and productivity engine built for high performers.
            Your absolute goal is to convert vague user inputs into structured, outcome-based missions.
            
            ALWAYS:
            - Convert vague goals into definitive outcome-based "missions" with strong success criteria.
            - Identify difficulty: Easy (25 XP, <=30m), Medium (50 XP, 30m-2h), Hard (100 XP, 2h-5h), Epic (250 XP, multi-day or >5h).
            - Recommend 3 to 7 highly actionable, sequential subtasks that are self-contained.
            - Predict specific blockers and suggest resources.
            - Classify the mission into one of the following Skill Tree categories: Development, Learning, Career, Business, Fitness, Writing.
            
            Never be conversational or produce conversational wrapper text. Return JSON matching:
            {
               "missions": [
                  {
                     "title": "Short outcome-based title",
                     "refinedOutcome": "Specific measurable success criteria description",
                     "successCriteria": ["criterion 1", "criterion 2", "criterion 3"],
                     "subtasks": ["step 1", "step 2", "step 3"],
                     "estimatedMinutes": 120,
                     "difficulty": "Medium",
                     "category": "Development",
                     "predictedBlockers": "What will likely stop them?",
                     "recommendedResources": "Where can they seek help?"
                  }
               ]
            }
            Use the historical context if provided to adjust estimated minutes, difficulties, and recommended resources.
        """.trimIndent()

        val prompt = if (historicalContext.isNotEmpty()) {
            "Goals input: $goalsText\n\nUser Historical Performance Context: $historicalContext"
        } else {
            "Goals input: $goalsText"
        }

        val jsonString = callGeminiApi(systemMessage, prompt, jsonResponse = true)
        val adapter = moshi.adapter(AIMissionListResponse::class.java)
        return adapter.fromJson(jsonString) ?: throw IOException("Failed to parse mission enhancement JSON")
    }

    /**
     * Generates a concise Daily Brief under 150 words.
     */
    suspend fun generateDailyBrief(missionsListText: String): AIDailyBriefResponse {
        val systemMessage = """
            You are "Operator", an execution coach.
            Generate a highly concise briefing of today's missions under 150 words.
            Determine which mission is the Primary Mission (the one with the highest strategic value or impact).
            List other missions as secondary.
            Sum up estimated effort.
            Recommend a clear focus sequence order.
            Highlight the top predicted blocker.
            Return JSON matching:
            {
              "primaryMission": "Name of the single most important mission",
              "secondaryMissions": ["Mission title 2", "Mission title 3"],
              "totalEstimatedEffort": "e.g. 3 hours 30 mins",
              "recommendedFocusOrder": "Step by step execution sequence",
              "predictedBlockers": "Top predicted blockers for today",
              "briefingText": "Incredible, highly direct coaching message of under 100 words emphasizing focus and execution."
            }
        """.trimIndent()

        val jsonString = callGeminiApi(systemMessage, "Missions representing today's plan: $missionsListText", jsonResponse = true)
        val adapter = moshi.adapter(AIDailyBriefResponse::class.java)
        return adapter.fromJson(jsonString) ?: throw IOException("Failed to parse daily brief JSON")
    }

    /**
     * Context-aware Coaching Assistant during Focus Mode.
     */
    suspend fun getCoachAdvice(
        currentMission: String,
        activeSubtask: String,
        blockerHistory: String,
        userQuery: String
    ): AICoachAdviceResponse {
        val systemMessage = """
            You are "Operator", a context-aware AI Execution Coach.
            The user is currently in Focus Mode and needs direct, immediate help.
            Never behave like a generic chatbot.
            Do not write long motivational speeches.
            Prioritize execution instruction over explanation. Keep answers highly punchy and action-oriented.
            Return JSON matching:
            {
              "advice": "Extremely direct coaching feedback, actionable instructions, and immediate steps.",
              "suggestedSubtasks": ["Any broken down details or next mini-actions (1-2 words each) if needed", "another mini-task"],
              "recoveryTip": "A physical or mental tactical recovery tip to break through the current blocker (20 words max)."
            }
        """.trimIndent()

        val prompt = """
            Current Mission: $currentMission
            Active Subtask: $activeSubtask
            Previous Blocker History: $blockerHistory
            User Query/Issue: $userQuery
        """.trimIndent()

        val jsonString = callGeminiApi(systemMessage, prompt, jsonResponse = true)
        val adapter = moshi.adapter(AICoachAdviceResponse::class.java)
        return adapter.fromJson(jsonString) ?: throw IOException("Failed to parse coach advice JSON")
    }

    /**
     * Conducts Evening Debriefing analysis.
     */
    suspend fun runEveningDebriefAnalysis(
        completedMissions: String,
        failedMissions: String,
        failureReasons: String,
        energyLevel: Int
    ): AIDebriefResponse {
        val systemMessage = """
            You are "Operator", an execution coach.
            The user has finished their day and is performing a daily debrief.
            Analyze what was completed, what was delayed or blocked, the stated reasons, and their energy level ($energyLevel/10).
            Provide:
            1. A clear direct summary of the day.
            2. Meaningful real-world lessons learned.
            3. Recommended actionable adjustments for tomorrow's planning.
            Never be vague or generic. Match the following JSON structured layout:
            {
              "summary": "Direct, data-driven synthesis of today's work.",
              "lessonsLearned": "Specific pattern observations based on their blockers and successes.",
              "suggestedAdjustments": "Specific advice for tomorrow's plan (e.g. adjust task difficulty, schedule writing during peaks, etc.)"
            }
        """.trimIndent()

        val prompt = """
            Completed Missions: $completedMissions
            Missions Not Completed/Delayed: $failedMissions
            Stated Reasons for Blockers: $failureReasons
            Energy Level reported: $energyLevel/10
        """.trimIndent()

        val jsonString = callGeminiApi(systemMessage, prompt, jsonResponse = true)
        val adapter = moshi.adapter(AIDebriefResponse::class.java)
        return adapter.fromJson(jsonString) ?: throw IOException("Failed to parse daily debrief JSON")
    }

    /**
     * Generates analytical insights from historical data.
     */
    suspend fun generateInsights(historicalMissionsText: String, focusSessionsText: String): AIInsightsResponse {
        val systemMessage = """
            You are "Operator", a professional productivity analyst.
            Review the historical logs of completed and failed missions and focus sessions.
            Generate 2 to 4 highly personalized, data-backed insights about their work habits.
            Focus on:
            - Completion rate per category (Development, Learning, Career, etc.)
            - Actual duration vs. estimated duration (estimation accuracy)
            - Streak progress
            - Most common blocker reasons
            Example outputs:
            - "Coding tasks (Development) are completed 90% more often in the morning."
            - "Research tasks (Learning) exceed estimates by 35%. Plan longer buffer blocks."
            - "Documentation confusion is your most common blocker, causing 4 delayed sessions."
            Return JSON matching:
            {
              "insights": [
                "Insight sentence 1",
                "Insight sentence 2"
              ]
            }
        """.trimIndent()

        val prompt = """
            Missions Log: $historicalMissionsText
            Focus Sessions Log: $focusSessionsText
        """.trimIndent()

        val jsonString = callGeminiApi(systemMessage, prompt, jsonResponse = true)
        val adapter = moshi.adapter(AIInsightsResponse::class.java)
        return adapter.fromJson(jsonString) ?: throw IOException("Failed to parse insights JSON")
    }
}
