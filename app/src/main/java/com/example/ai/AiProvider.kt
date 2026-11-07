package com.example.ai

/**
 * Supported AI Providers for semantic music search and tracklist vision matching.
 */
enum class AiProvider(
    val id: String,
    val displayNameFa: String,
    val displayNameEn: String,
    val shortName: String,
    val defaultModel: String,
    val placeholderKey: String,
    val helpUrl: String,
    val prefKey: String
) {
    GEMINI(
        id = "gemini",
        displayNameFa = "گوگل جمنای (Gemini)",
        displayNameEn = "Google Gemini",
        shortName = "Gemini",
        defaultModel = "gemini-3.5-flash",
        placeholderKey = "AIzaSy...",
        helpUrl = "aistudio.google.com/apikey",
        prefKey = "to_ot_custom_gemini_api_key"
    ),
    OPENAI(
        id = "openai",
        displayNameFa = "اوپن‌ای‌آی (ChatGPT)",
        displayNameEn = "OpenAI (ChatGPT)",
        shortName = "OpenAI",
        defaultModel = "gpt-4o-mini",
        placeholderKey = "sk-proj-...",
        helpUrl = "platform.openai.com/api-keys",
        prefKey = "to_ot_custom_openai_api_key"
    ),
    DEEPSEEK(
        id = "deepseek",
        displayNameFa = "دیپ‌سیک (DeepSeek)",
        displayNameEn = "DeepSeek AI",
        shortName = "DeepSeek",
        defaultModel = "deepseek-chat",
        placeholderKey = "sk-...",
        helpUrl = "platform.deepseek.com",
        prefKey = "to_ot_custom_deepseek_api_key"
    ),
    GROQ(
        id = "groq",
        displayNameFa = "گراک (Groq فوق‌سریع)",
        displayNameEn = "Groq (Ultra-Fast)",
        shortName = "Groq",
        defaultModel = "llama-3.3-70b-versatile",
        placeholderKey = "gsk_...",
        helpUrl = "console.groq.com/keys",
        prefKey = "to_ot_custom_groq_api_key"
    );

    companion object {
        fun fromId(id: String?): AiProvider = entries.find { it.id.equals(id, ignoreCase = true) } ?: GEMINI
    }
}
