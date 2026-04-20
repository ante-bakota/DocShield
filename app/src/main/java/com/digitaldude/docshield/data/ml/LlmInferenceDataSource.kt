package com.digitaldude.docshield.data.ml

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Data source that runs Gemma 3 1B on-device via LiteRT-LM API.
 *
 * Role in the AI fallback chain: PRIMARY categorization source.
 * Accepts any text length and answers arbitrary prompts — suitable for
 * document classification unlike GeminiNano which only summarizes.
 *
 * Model (.litertlm) is pushed manually to device storage via adb.
 * Engine is initialized once and kept alive — loading a 700MB model
 * is expensive and should not be repeated per request.
 *
 * Thread safety: Engine is NOT thread-safe. All calls run on Dispatchers.IO.
 */

class LlmInferenceDataSource(private val context : Context) {

    private var engine : Engine? = null

    companion object{
        private const val TAG = "Llm inference"
        private const val MODEL_FILENAME = "gemma3-1b-it-int4.litertlm" //smaller model, we will check how it performs and add bigger one if necessary
      //  private const val MODEL_URL = "model url here"

    //    /data/data/com.digitaldude.docshield/files/gemma3-1b-it-int4.litertlm
        private val CATEGORY_MAP = mapOf(
            "Invoice"  to "Racun",
            "Health"   to "Zdravlje",
            "Identity" to "Osobne isprave",
            "Other"    to "Ostalo"
        )

    }

    /**
     * Checks if model file exists on device and initializes the Engine.
     * Returns false if model has not been pushed via adb yet.
     */
    suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!modelFileExists()) {
                Log.w(TAG, "Model file not found: push via adb first")
                return@withContext false
            }
            initializeEngine()
        } catch (e: Exception) {
            Log.e(TAG, "isAvailable failed: ${e.message}", e)
            false
        }
    }

    private fun modelFileExists() : Boolean{
        return File(context.filesDir, MODEL_FILENAME).exists()
    }
//
//    private suspend fun downloadModel() : Boolean = withContext(Dispatchers.IO){
//        val outputFile = File(context.filesDir, MODEL_FILENAME)
//        try {
//            val connection = URL(MODEL_URL).openConnection() as HttpURLConnection
//            connection.connect()
//            if(connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext false
//
//            connection.getInputStream().use { inputStream ->
//                FileOutputStream(outputFile).use { output ->
//                    inputStream.copyTo(output)
//                }
//            }
//            Log.d(TAG, "Model downloaded to ${outputFile.absolutePath}")
//            true
//        }catch (e: Exception){
//            Log.e(TAG, "Download failed: ${e.message}", e)
//            outputFile.delete() // clean up partial download
//            false        }
//    }

    private fun initializeEngine(): Boolean {
        val modelPath = File(context.filesDir, MODEL_FILENAME).absolutePath
        return try {
            val cpuConfig = EngineConfig(modelPath = modelPath, backend = Backend.CPU())
            engine = Engine(cpuConfig).also { it.initialize() }
            Log.d(TAG, "Engine initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Engine init failed: ${e.message}", e)
            false
        }
    }
//    private fun initializeLlm() : Boolean{
//            return try {
//                val modelPath = File(context.filesDir, MODEL_FILENAME).absolutePath
//                val option = LlmInference.LlmInferenceOptions.builder()
//                    .setModelPath(modelPath)
//                    .setMaxTokens(50)
//                    .build()
//                llmInference = LlmInference.createFromOptions(context, option)
//                Log.d(TAG, "LlmInference initialized successfully")
//                true
//            }catch (e : Exception){
//                Log.e(TAG, "LlmInference init failed: ${e.message}", e)
//                false
//            }
//    }

    private fun buildPrompt(text : String) : String {
        val truncated = text.take(500)
        return """
              You are a document classifier. Respond with ONLY one category name, nothing else.
              Categories: ${CATEGORY_MAP.keys.joinToString(", ")}

              Document text:
              $truncated

              Category:
          """.trimIndent()
    }

    /**
     * Classifies document text using Gemma 3 on-device.
     * Collects streamed tokens into a full response before parsing category.
     */
    suspend fun categorize(extractedText: String): String? = withContext(Dispatchers.IO) {
        val currentEngine = engine ?: return@withContext null
        try {
            currentEngine.createConversation().use { conversation ->
                val prompt = buildPrompt(extractedText)
                val responseBuilder = StringBuilder()
                conversation.sendMessageAsync(prompt).collect { token ->
                    responseBuilder.append(token)
                }
                Log.d("test", responseBuilder.toString())
                parseCategory(responseBuilder.toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "categorize failed: ${e.message}", e)
            null
        }
    }


    /**
     * Parses the raw model response and maps it to a known category.
     * Models sometimes return extra whitespace or punctuation — we strip and match loosely.
     */
    private fun parseCategory(response: String): String {
        val cleaned = response.trim().trimEnd('.', ',', '!', '?')
        return CATEGORY_MAP.entries.firstOrNull { (englishKey, _) ->
            cleaned.contains(englishKey, ignoreCase = true)
        }?.value ?: "Ostalo"
    }

    /** Releases model resources. Call from ViewModel.onCleared(). */
    /** Releases Engine resources. Call from ViewModel.onCleared(). */
    fun close() {
        engine?.close()
        engine = null
    }

}