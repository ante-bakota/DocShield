import android.content.Context
import android.util.Log
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.Summarizer
import com.google.mlkit.genai.summarization.SummarizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.guava.await


/**
 * Data source that accesses Gemini Nano via the ML Kit Summarization API.
 *
 * Important device limitation: The ML Kit Prompt API (free-form prompts) currently
 * supports only Pixel 9 series and Galaxy Z Fold7. The Summarization API has
 * broader support, including Samsung Galaxy S25, which is why we use it here.
 *
 * Role in the fallback chain: Gemini Nano is responsible only for generating
 * a meaningful document title from OCR text. Categorization is handled by
 * MediaPipe (LlmInferenceDataSource) or the rule-based fallback.
 *
 * The model lives in Android AICore (system partition) — this class never
 * downloads or bundles any model files.
 */
class GeminiNanoDataSource(private val context: Context) {

    private var summarizer: Summarizer? = null

    /**
     * Checks whether the Summarization API is available on this device.
     * If status is DOWNLOADABLE, triggers a one-time model download.
     * Returns true only when the model is ready to use.
     */
    suspend fun isAvailable(): Boolean {
        return try {
            val options = SummarizerOptions.builder(context)
                .setInputType(SummarizerOptions.InputType.ARTICLE)
                .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
                .setLanguage(SummarizerOptions.Language.ENGLISH)
                .setLongInputAutoTruncationEnabled(true)
                .build()



            val client = Summarization.getClient(options)
            val status = client.checkFeatureStatus().await()
            Log.d("GeminiNano", "FeatureStatus: $status")  // <-- dodaj ovo


            when (client.checkFeatureStatus().await()) {
                FeatureStatus.AVAILABLE -> {
                    summarizer = client
                    true
                }
                FeatureStatus.DOWNLOADABLE -> {
                    downloadAndWait(client)
                }
                else -> false  // DOWNLOADING or UNAVAILABLE
            }
        } catch (e: Exception) {
            Log.e("GeminiNano", "Exception: ${e.message}", e)  // <-- i ovo

            false
        }
    }

    /**
     * Triggers model download and suspends until it completes or fails.
     * Uses suspendCancellableCoroutine to bridge the callback-based DownloadCallback
     * into a suspend function — the same pattern as old AsyncTask/callbacks converted to coroutines.
     */
    private suspend fun downloadAndWait(client: Summarizer): Boolean {
        return suspendCancellableCoroutine { continuation ->
            client.downloadFeature(object : DownloadCallback {
                override fun onDownloadStarted(bytesToDownload: Long) {}
                override fun onDownloadProgress(totalBytesDownloaded: Long) {}
                override fun onDownloadCompleted() {
                    summarizer = client
                    continuation.resumeWith(Result.success(true))
                }
                override fun onDownloadFailed(e: GenAiException) {
                    continuation.resumeWith(Result.success(false))
                }
            })
        }
    }

    /**
     * Summarizes the OCR text using Gemini Nano and returns it as a suggested title.
     * Throws if called before [isAvailable] returns true.
     */
    suspend fun suggestTitle(extractedText: String): String? {
        val client = summarizer
            ?: throw IllegalStateException("Summarizer not initialized. Call isAvailable() first.")

        val request = SummarizationRequest.builder(extractedText).build()
        val result = client.runInference(request).await()

        return result.getSummary()
            .trim()
            .takeIf { it.isNotBlank() }
    }

    /** Releases the underlying model resources. Call from ViewModel.onCleared(). */
    fun close() {
        summarizer?.close()
        summarizer = null
    }
}
