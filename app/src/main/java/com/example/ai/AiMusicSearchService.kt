package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.DeviceAudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Intelligent Music Search Service powered by Gemini AI and smart offline Persian/English
 * phonetic transliteration.
 *
 * Capabilities:
 * 1. Semantic Mood & Natural Language search (e.g. "موزیک های شاد", "آرامش بخش برای خواب", "ورزشی").
 * 2. Automatic bilingual transliteration (e.g. "داریوش" matches "Dariush", "Daryoush", etc.).
 * 3. Multimodal Image Search: extracts song/artist names from tracklists, concert flyers, or
 *    playlist screenshots and finds matching songs in the phone's audio library.
 */
class AiMusicSearchService(private val context: Context) {

    companion object {
        private const val TAG = "AiMusicSearchService"
        private const val GEMINI_MODEL = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

        // Persian-English transliterations mapping for popular artists & terms
        private val COMMON_TRANSLITERATIONS = mapOf(
            "داریوش" to listOf("dariush", "daryoush", "darioush", "daryoosh", "daryush"),
            "داریوش اقبالی" to listOf("dariush eghbali", "dariush", "daryoush"),
            "ابی" to listOf("ebi", "ebrahim hamedi"),
            "معین" to listOf("moein", "moen", "moeen", "nasrollah moein"),
            "شادمهر" to listOf("shadmehr", "aghili", "shadmehr aghili"),
            "شادمهر عقیلی" to listOf("shadmehr aghili", "shadmehr"),
            "هایده" to listOf("hayedeh", "haideh", "haedeh"),
            "مهستی" to listOf("mahasti", "mahasty"),
            "گوگوش" to listOf("googoosh", "gugush", "faegheh atashin"),
            "حمیرا" to listOf("homeyra", "homeira", "homayra"),
            "سیاوش" to listOf("siavash", "ghomayshi", "ghomeyshi", "siavash ghomayshi"),
            "قمیشی" to listOf("ghomayshi", "ghomeyshi", "siavash"),
            "سیاوش قمیشی" to listOf("siavash ghomayshi", "ghomayshi", "siavash"),
            "چاوشی" to listOf("chavoshi", "mohsen chavoshi"),
            "محسن چاوشی" to listOf("mohsen chavoshi", "chavoshi"),
            "محسن یگانه" to listOf("mohsen yeganeh", "yeganeh"),
            "محسن نامجو" to listOf("mohsen namjoo", "namjoo"),
            "حبیب" to listOf("habib", "habib mohebian"),
            "شجریان" to listOf("shajarian", "homayoun", "mohammad reza", "homayun"),
            "همایون" to listOf("homayoun", "shajarian", "homayun"),
            "همایون شجریان" to listOf("homayoun shajarian", "homayoun", "shajarian"),
            "یاس" to listOf("yas", "yaser"),
            "هیچکس" to listOf("hichkas", "soroush lashgari"),
            "پیشرو" to listOf("pishro", "reza pishro"),
            "بهرام" to listOf("bahram", "bahram nouraei"),
            "حصین" to listOf("hocein", "hosein"),
            "تتلو" to listOf("tataloo", "tatallo", "amirtataloo"),
            "زدبازی" to listOf("zedbazi", "zed bazi"),
            "مهراد هیدن" to listOf("mehrad hidden", "hidden"),
            "سهراب ام جی" to listOf("sohrab mj"),
            "علیرضا جی جی" to listOf("alireza jj", "jj"),
            "سیجل" to listOf("sijal"),
            "وانتونز" to listOf("wantons", "the wantons"),
            "پوبون" to listOf("poobon"),
            "عرفان" to listOf("erfan", "paydar"),
            "سوگند" to listOf("sogand", "sougand"),
            "رضا بهرام" to listOf("reza bahram", "bahram"),
            "بهنام بانی" to listOf("behnam bani", "bani"),
            "سیروان" to listOf("sirvan", "khosravi", "sirvan khosravi"),
            "سیروان خسروی" to listOf("sirvan khosravi", "sirvan"),
            "زانیار" to listOf("xaniar", "zaniar", "xaniar khosravi"),
            "زانیار خسروی" to listOf("xaniar khosravi", "zaniar", "xaniar"),
            "فرزاد فرزین" to listOf("farzad farzin", "farzin"),
            "محسن ابراهیم زاده" to listOf("mohsen ebrahimzadeh", "ebrahimzadeh"),
            "طلیسچی" to listOf("talischi", "alireza talischi"),
            "علیرضا طلیسچی" to listOf("alireza talischi", "talischi"),
            "قربانی" to listOf("ghorbani", "alireza ghorbani"),
            "علیرضا قربانی" to listOf("alireza ghorbani", "ghorbani"),
            "عقیلی" to listOf("aghili", "salar aghili"),
            "سالار عقیلی" to listOf("salar aghili", "aghili"),
            "خواجه امیری" to listOf("khajehamiri", "ehsan khajehamiri"),
            "احسان خواجه امیری" to listOf("ehsan khajehamiri", "khajehamiri"),
            "ماکان بند" to listOf("macan band", "macan"),
            "هوروش بند" to listOf("hoorosh band", "hoorosh"),
            "پازل بند" to listOf("puzzle band"),
            "ستار" to listOf("sattar"),
            "اندی" to listOf("andy"),
            "شهرام شب پره" to listOf("shahram shabpareh", "shabpareh"),
            "شهرام صولتی" to listOf("shahram solati", "solati"),
            "شهره" to listOf("shohreh"),
            "لیلا فروهر" to listOf("leila forouhar", "forouhar"),
            "عارف" to listOf("aref"),
            "فرامرز اصلانی" to listOf("faramarz aslani", "aslani"),
            "کوروش یغمایی" to listOf("kourosh yaghmaei", "yaghmaei"),
            "فرهاد" to listOf("farhad", "farhad mehrad"),
            "فریدون فروغی" to listOf("fereydoon foroughi", "foroughi"),
            "رضا صادقی" to listOf("reza sadeghi", "sadeghi"),
            "بابک جهانبخش" to listOf("babak jahanbakhsh", "jahanbakhsh"),
            "بنیامین" to listOf("benyamin", "bahadori"),
            "مازیار فلاحی" to listOf("mazyar fallahi", "fallahi"),
            "علی یاسینی" to listOf("ali yasini", "yasini"),
            "روزبه بمانی" to listOf("roozbeh bemani", "bemani"),
            "کسری زاهدی" to listOf("kasra zahedi", "zahedi"),
            "راغب" to listOf("ragheb"),
            "محمد علیزاده" to listOf("mohammad alizadeh", "alizadeh"),
            "سامان جلیلی" to listOf("saman jalili", "jalili"),
            "امید حاجیلی" to listOf("omid hajili", "hajili"),
            "ایهام" to listOf("ehaam", "eham"),
            "اشوان" to listOf("ashvan"),
            "شروین" to listOf("shervin", "shervin hajipour"),
            "سهراب پاکزاد" to listOf("sohrab pakzad", "pakzad"),
            "آرمین" to listOf("armin", "2afm", "armin zarei")
        )

        // Mood keywords for semantic matching
        private val MOOD_KEYWORDS = mapOf(
            "شاد" to listOf("remix", "dance", "shad", "party", "bass", "club", "happy", "dj", "دنس", "شاد", "ریمیکس", "upbeat", "energetic"),
            "رقص" to listOf("remix", "dance", "shad", "party", "club", "dj", "دنس", "رقص", "بندری", "عروسی"),
            "رقصی" to listOf("remix", "dance", "shad", "party", "club", "dj", "دنس", "رقص"),
            "آرام" to listOf("chill", "relax", "sleep", "lofi", "acoustic", "slow", "soft", "calm", "aram", "آرام", "ملایم", "peaceful", "light"),
            "آرامش" to listOf("chill", "relax", "sleep", "lofi", "acoustic", "slow", "soft", "calm", "آرامش", "ملایم"),
            "خواب" to listOf("chill", "relax", "sleep", "night", "soft", "calm", "lofi", "خواب", "شب"),
            "مطالعه" to listOf("lofi", "chill", "instrumental", "study", "ambient", "focus", "تمرکز"),
            "ورزش" to listOf("workout", "gym", "energy", "trap", "edm", "bass", "fitness", "remix", "ورزش", "باشگاه"),
            "ورزشی" to listOf("workout", "gym", "energy", "trap", "edm", "bass", "fitness", "remix", "ورزشی"),
            "پرانرژی" to listOf("energy", "bass", "workout", "trap", "edm", "remix", "dance", "انرژی", "power"),
            "غمگین" to listOf("sad", "slow", "ghamgin", "del", "acoustic", "dep", "غمگین", "احساسی", "دلشکسته", "تنهایی"),
            "دپ" to listOf("sad", "slow", "dep", "ghamgin", "acoustic"),
            "بیکلام" to listOf("instrumental", "ambient", "piano", "guitar", "soundtrack", "بی کلام"),
            "سنتی" to listOf("sonati", "traditional", "tar", "setar", "santur", "shajarian", "sonati")
        )
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Retrieves the currently active AI provider (defaults to Gemini).
     */
    fun getActiveProvider(): AiProvider {
        val prefs = context.getSharedPreferences("to_ot_player_prefs", Context.MODE_PRIVATE)
        return AiProvider.fromId(prefs.getString("to_ot_active_ai_provider", AiProvider.GEMINI.id))
    }

    /**
     * Sets the active AI provider in SharedPreferences.
     */
    fun setActiveProvider(provider: AiProvider) {
        val prefs = context.getSharedPreferences("to_ot_player_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("to_ot_active_ai_provider", provider.id).apply()
    }

    /**
     * Gets the custom saved API key for a specific AI provider.
     */
    fun getApiKeyForProvider(provider: AiProvider): String? {
        val prefs = context.getSharedPreferences("to_ot_player_prefs", Context.MODE_PRIVATE)
        return prefs.getString(provider.prefKey, null)?.trim()?.takeIf { it.isNotEmpty() }
    }

    /**
     * Saves or clears the API key for a specific AI provider.
     */
    fun saveApiKeyForProvider(provider: AiProvider, key: String?) {
        val prefs = context.getSharedPreferences("to_ot_player_prefs", Context.MODE_PRIVATE)
        val cleanKey = key?.trim()
        if (cleanKey.isNullOrEmpty()) {
            prefs.edit().remove(provider.prefKey).apply()
        } else {
            prefs.edit().putString(provider.prefKey, cleanKey).apply()
        }
    }

    /**
     * Retrieves the custom Gemini API key (backward compatible).
     */
    fun getCustomApiKey(): String? = getApiKeyForProvider(AiProvider.GEMINI)

    /**
     * Persists or clears the custom Gemini API key (backward compatible).
     */
    fun saveCustomApiKey(key: String?) = saveApiKeyForProvider(AiProvider.GEMINI, key)

    /**
     * Returns the active API key for a provider.
     * For Gemini, user key takes priority, then BuildConfig.GEMINI_API_KEY.
     * For other providers (OpenAI, DeepSeek, Groq), uses user custom key.
     */
    fun getEffectiveApiKey(provider: AiProvider = getActiveProvider()): String {
        val custom = getApiKeyForProvider(provider)
        if (!custom.isNullOrBlank()) {
            return custom
        }
        if (provider == AiProvider.GEMINI) {
            val buildKey = BuildConfig.GEMINI_API_KEY
            return if (!buildKey.isNullOrBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
        }
        return ""
    }

    /**
     * Checks whether a valid API key is available for the given provider.
     */
    fun hasValidApiKey(provider: AiProvider = getActiveProvider()): Boolean {
        val key = getEffectiveApiKey(provider)
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Returns true if user configured a manual API key for the given provider.
     */
    fun isUsingCustomApiKey(provider: AiProvider = getActiveProvider()): Boolean {
        return !getApiKeyForProvider(provider).isNullOrBlank()
    }

    /**
     * Fast local bilingual expansion for search queries:
     * If user types "داریوش", expands to include ["داریوش", "dariush", "daryoush", "darioush", ...].
     * If user types "dariush", expands to include ["dariush", "داریوش"].
     * If user types "موزیک های شاد", expands to include dance/remix/happy/upbeat terms.
     */
    fun expandQueryTerms(rawQuery: String): List<String> {
        val trimmed = rawQuery.trim().lowercase(Locale.getDefault())
        if (trimmed.isEmpty()) return emptyList()

        val results = mutableSetOf(trimmed)

        // Check full query first
        for ((persian, englishList) in COMMON_TRANSLITERATIONS) {
            val pLower = persian.lowercase(Locale.getDefault())
            if (trimmed.contains(pLower) || pLower.contains(trimmed)) {
                results.add(persian)
                results.addAll(englishList)
            }
            for (eng in englishList) {
                if (trimmed.contains(eng) || eng.contains(trimmed)) {
                    results.add(persian)
                    results.addAll(englishList)
                }
            }
        }

        // Check full query for mood keywords
        for ((mood, keywords) in MOOD_KEYWORDS) {
            if (trimmed.contains(mood)) {
                results.addAll(keywords)
            }
        }

        // Tokenize and check individual words
        val tokens = trimmed.split(Regex("[\\s,،_\\-./]+")).filter { it.length >= 2 }
        for (token in tokens) {
            for ((persian, englishList) in COMMON_TRANSLITERATIONS) {
                val p = persian.lowercase(Locale.getDefault())
                if (token == p || token.contains(p) || p.contains(token)) {
                    results.add(persian)
                    results.addAll(englishList)
                }
                for (eng in englishList) {
                    if (token == eng || token.contains(eng) || eng.contains(token)) {
                        results.add(persian)
                        results.addAll(englishList)
                    }
                }
            }
            for ((mood, keywords) in MOOD_KEYWORDS) {
                if (token.contains(mood) || mood.contains(token)) {
                    results.addAll(keywords)
                }
            }
            val tokenTranslit = transliteratePersianToFingilish(token)
            if (tokenTranslit.isNotEmpty() && tokenTranslit != token) {
                results.add(tokenTranslit)
            }
        }

        // Basic character-by-character transliteration of the whole query
        val transliterated = transliteratePersianToFingilish(trimmed)
        if (transliterated.isNotEmpty() && transliterated != trimmed) {
            results.add(transliterated)
        }

        return results.toList()
    }

    /**
     * Performs a fast local offline match using expanded search terms.
     */
    fun matchOffline(query: String, tracks: List<DeviceAudioFile>): List<Long> {
        val terms = expandQueryTerms(query)
        if (terms.isEmpty()) return emptyList()

        return tracks.filter { track ->
            val titleLower = track.title.lowercase(Locale.getDefault())
            val artistLower = (track.artist ?: "").lowercase(Locale.getDefault())
            val displayLower = track.displayName.lowercase(Locale.getDefault())

            terms.any { term ->
                titleLower.contains(term) || artistLower.contains(term) || displayLower.contains(term)
            }
        }.map { it.id }
    }

    /**
     * Performs semantic / natural-language search via the active AI provider
     * (Gemini, OpenAI, DeepSeek, or Groq).
     * Takes user query + list of device tracks and returns the matched track IDs.
     */
    suspend fun searchWithAi(
        query: String,
        tracks: List<DeviceAudioFile>
    ): Result<List<Long>> = withContext(Dispatchers.IO) {
        val provider = getActiveProvider()
        val apiKey = getEffectiveApiKey(provider)
        if (!hasValidApiKey(provider)) {
            // Fall back cleanly to smart offline matching
            val offlineMatches = matchOffline(query, tracks)
            return@withContext Result.success(offlineMatches)
        }

        if (tracks.isEmpty()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val matchedIds = when (provider) {
                AiProvider.GEMINI -> searchWithGemini(apiKey, query, tracks)
                AiProvider.OPENAI -> searchWithOpenAiCompatible(
                    provider = provider,
                    endpoint = "https://api.openai.com/v1/chat/completions",
                    apiKey = apiKey,
                    query = query,
                    tracks = tracks
                )
                AiProvider.DEEPSEEK -> searchWithOpenAiCompatible(
                    provider = provider,
                    endpoint = "https://api.deepseek.com/chat/completions",
                    apiKey = apiKey,
                    query = query,
                    tracks = tracks
                )
                AiProvider.GROQ -> searchWithOpenAiCompatible(
                    provider = provider,
                    endpoint = "https://api.groq.com/openai/v1/chat/completions",
                    apiKey = apiKey,
                    query = query,
                    tracks = tracks
                )
            }

            // If the model returned an empty list or couldn't match, fallback to offline matcher
            if (matchedIds.isEmpty()) {
                val fallback = matchOffline(query, tracks)
                if (fallback.isNotEmpty()) {
                    return@withContext Result.success(fallback)
                }
            }

            Result.success(matchedIds)
        } catch (e: Exception) {
            Log.e(TAG, "Error in searchWithAi ($provider): $e")
            Result.success(matchOffline(query, tracks))
        }
    }

    private fun searchWithGemini(
        apiKey: String,
        query: String,
        tracks: List<DeviceAudioFile>
    ): List<Long> {
        val tracksSummary = buildTracksSummaryJson(tracks)
        val promptText = """
            The user wants to search their phone's local audio library for:
            Query: "$query"

            Here is the list of available audio tracks on their phone:
            $tracksSummary

            Instructions:
            1. Match tracks that correspond to the query.
            2. Consider Persian & English transliterations (e.g. داریوش matches tracks with Dariush, Daryoush, داریوش).
            3. If the query expresses a mood, situation, or genre (e.g. "موزیک های شاد", "آرامش بخش", "ورزشی", "رقصی", "remix"), match songs whose title, artist, or style fit that mood.
            4. Return ONLY a valid JSON array of matching track IDs, e.g. [101, 102]. If none match, return [].
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            })
        }

        val endpoint = "$BASE_URL$GEMINI_MODEL:generateContent?key=$apiKey"
        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "HTTP ${response.code}"
            Log.w(TAG, "Gemini text search failed: $errorBody.")
            return emptyList()
        }

        val responseBody = response.body?.string().orEmpty()
        return parseGeminiIdArray(responseBody)
    }

    private fun searchWithOpenAiCompatible(
        provider: AiProvider,
        endpoint: String,
        apiKey: String,
        query: String,
        tracks: List<DeviceAudioFile>
    ): List<Long> {
        val tracksSummary = buildTracksSummaryJson(tracks)
        val promptText = """
            Query: "$query"

            Tracks on device:
            $tracksSummary
        """.trimIndent()

        val systemPrompt = "You are an intelligent music search assistant. You will be given a user search query (which may be in Persian, English, or transliterated, or specify a mood/genre) and a list of audio files on their device as JSON [{id, title, artist}]. Output ONLY a valid JSON array of matching track IDs, for example [101, 102]. If none match, output []. Do NOT output any explanation, markdown backticks, or other text."

        val requestJson = JSONObject().apply {
            put("model", provider.defaultModel)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", promptText)
                })
            })
            put("temperature", 0.1)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "HTTP ${response.code}"
            Log.w(TAG, "${provider.displayNameEn} text search failed: $errorBody.")
            return emptyList()
        }

        val responseBody = response.body?.string().orEmpty()
        return parseChatCompletionIdArray(responseBody)
    }

    /**
     * Performs multimodal Image Search:
     * User provides an image (e.g. photo of playlist, concert flyer, album cover, handwritten list),
     * and Gemini identifies song titles / artist names in the image and matches them against the phone's tracks.
     */
    suspend fun searchWithImage(
        imageUri: Uri,
        tracks: List<DeviceAudioFile>
    ): Result<List<Long>> = withContext(Dispatchers.IO) {
        val activeProvider = getActiveProvider()

        // Vision routing: OpenAI and Gemini support visual recognition.
        val (visionProvider, visionKey) = when {
            activeProvider == AiProvider.OPENAI && hasValidApiKey(AiProvider.OPENAI) -> {
                Pair(AiProvider.OPENAI, getEffectiveApiKey(AiProvider.OPENAI))
            }
            hasValidApiKey(AiProvider.GEMINI) -> {
                Pair(AiProvider.GEMINI, getEffectiveApiKey(AiProvider.GEMINI))
            }
            hasValidApiKey(AiProvider.OPENAI) -> {
                Pair(AiProvider.OPENAI, getEffectiveApiKey(AiProvider.OPENAI))
            }
            else -> {
                return@withContext Result.failure(
                    IllegalStateException("برای جستجوی تصویری، لطفاً کلید Google Gemini یا OpenAI را در تنظیمات وارد کنید.")
                )
            }
        }

        if (tracks.isEmpty()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val bitmap = loadAndScaleBitmap(imageUri, maxDimension = 1024)
                ?: return@withContext Result.failure(IllegalArgumentException("امکان باز کردن تصویر وجود ندارد."))

            val matchedIds = if (visionProvider == AiProvider.OPENAI) {
                searchImageWithOpenAi(visionKey, bitmap, tracks)
            } else {
                searchImageWithGemini(visionKey, bitmap, tracks)
            }

            Result.success(matchedIds)
        } catch (e: Exception) {
            Log.e(TAG, "Error in searchWithImage: $e")
            Result.failure(e)
        }
    }

    private fun searchImageWithGemini(
        apiKey: String,
        bitmap: Bitmap,
        tracks: List<DeviceAudioFile>
    ): List<Long> {
        return try {
            val base64Data = bitmapToBase64(bitmap)
            val tracksSummary = buildTracksSummaryJson(tracks)

            val promptText = """
                Inspect this image carefully. It may contain a playlist screenshot, a handwritten or printed tracklist, concert flyer, or album cover.
                1. Extract all music song titles, track names, and artist names visible in the image.
                2. Match the extracted songs against the user's audio tracks below:
                $tracksSummary
                3. Return ONLY a valid JSON array of matching track IDs, e.g. [101, 102]. If none match, return [].
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Data)
                                })
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val endpoint = "$BASE_URL$GEMINI_MODEL:generateContent?key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "HTTP ${response.code}"
                Log.w(TAG, "Gemini image search failed: $errorBody")
                return emptyList()
            }

            val responseBody = response.body?.string().orEmpty()
            parseGeminiIdArray(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "Error in searchImageWithGemini: $e")
            emptyList()
        }
    }

    private fun searchImageWithOpenAi(
        apiKey: String,
        bitmap: Bitmap,
        tracks: List<DeviceAudioFile>
    ): List<Long> {
        return try {
            val base64Data = bitmapToBase64(bitmap)
            val tracksSummary = buildTracksSummaryJson(tracks)

            val promptText = """
                Inspect this image carefully (it could be a playlist screenshot, tracklist, album cover, or concert flyer).
                1. Extract all song titles, track names, and artist names visible.
                2. Match them against the available audio tracks below:
                $tracksSummary
                3. Return ONLY a valid JSON array of matching track IDs, e.g. [101, 102]. If none match, return []. Do NOT include explanation.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", promptText)
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$base64Data")
                                })
                            })
                        })
                    })
                })
                put("temperature", 0.1)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "HTTP ${response.code}"
                Log.w(TAG, "OpenAI image search failed: $errorBody")
                return emptyList()
            }

            val responseBody = response.body?.string().orEmpty()
            parseChatCompletionIdArray(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "Error in searchImageWithOpenAi: $e")
            emptyList()
        }
    }

    private fun parseChatCompletionIdArray(rawJsonResponse: String): List<Long> {
        return try {
            val root = JSONObject(rawJsonResponse)
            val choices = root.optJSONArray("choices") ?: return emptyList()
            val firstChoice = choices.optJSONObject(0) ?: return emptyList()
            val message = firstChoice.optJSONObject("message") ?: return emptyList()
            val content = message.optString("content").trim()

            val cleanJson = if (content.startsWith("```")) {
                content.substringAfter("\n").substringBeforeLast("```").trim()
            } else {
                content
            }

            val startIdx = cleanJson.indexOf('[')
            val endIdx = cleanJson.lastIndexOf(']')
            val arrayStr = if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                cleanJson.substring(startIdx, endIdx + 1)
            } else {
                cleanJson
            }

            val jsonArray = JSONArray(arrayStr)
            val results = mutableListOf<Long>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.opt(i)
                when (item) {
                    is Number -> results.add(item.toLong())
                    is String -> item.toLongOrNull()?.let { results.add(it) }
                }
            }
            results
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse OpenAI-compatible response JSON: $e, raw: $rawJsonResponse")
            emptyList()
        }
    }

    private fun buildTracksSummaryJson(tracks: List<DeviceAudioFile>): String {
        val array = JSONArray()
        // Take up to 250 tracks to keep token usage lean and fast
        for (track in tracks.take(250)) {
            val obj = JSONObject().apply {
                put("id", track.id)
                put("title", track.title)
                if (!track.artist.isNullOrBlank()) put("artist", track.artist)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseGeminiIdArray(rawJsonResponse: String): List<Long> {
        return try {
            val root = JSONObject(rawJsonResponse)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            val firstCandidate = candidates.optJSONObject(0) ?: return emptyList()
            val content = firstCandidate.optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            val textPart = parts.optJSONObject(0)?.optString("text").orEmpty().trim()

            // Text could be formatted as markdown code block ```json [...] ``` or bare [...]
            val cleanJson = if (textPart.startsWith("```")) {
                textPart.substringAfter("\n").substringBeforeLast("```").trim()
            } else {
                textPart
            }

            val jsonArray = JSONArray(cleanJson)
            val results = mutableListOf<Long>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.opt(i)
                when (item) {
                    is Number -> results.add(item.toLong())
                    is String -> item.toLongOrNull()?.let { results.add(it) }
                }
            }
            results
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse Gemini response JSON: $e, raw: $rawJsonResponse")
            emptyList()
        }
    }

    private fun loadAndScaleBitmap(uri: Uri, maxDimension: Int): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            var inSampleSize = 1
            while (boundsOptions.outWidth / inSampleSize > maxDimension || boundsOptions.outHeight / inSampleSize > maxDimension) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bitmap loading error: $e")
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun transliteratePersianToFingilish(text: String): String {
        val builder = java.lang.StringBuilder()
        for (ch in text) {
            when (ch) {
                'ا', 'آ' -> builder.append("a")
                'ب' -> builder.append("b")
                'پ' -> builder.append("p")
                'ت', 'ط' -> builder.append("t")
                'ث', 'س', 'ص' -> builder.append("s")
                'ج' -> builder.append("j")
                'چ' -> builder.append("ch")
                'ح', 'ه' -> builder.append("h")
                'خ' -> builder.append("kh")
                'د' -> builder.append("d")
                'ذ', 'ز', 'ض', 'ظ' -> builder.append("z")
                'ر' -> builder.append("r")
                'ژ' -> builder.append("zh")
                'ش' -> builder.append("sh")
                'ع' -> builder.append("a")
                'غ', 'ق' -> builder.append("gh")
                'ف' -> builder.append("f")
                'ک' -> builder.append("k")
                'گ' -> builder.append("g")
                'ل' -> builder.append("l")
                'م' -> builder.append("m")
                'ن' -> builder.append("n")
                'و' -> builder.append("o")
                'ی' -> builder.append("i")
                else -> builder.append(ch)
            }
        }
        return builder.toString()
    }
}
