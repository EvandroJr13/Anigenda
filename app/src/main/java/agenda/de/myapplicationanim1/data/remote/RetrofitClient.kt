package agenda.de.myapplicationanim1.data.remote

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class RateLimitRetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var tryCount = 0
        val maxRetries = 2

        while (true) {
            try {
                val response = chain.proceed(request)
                if (response.code == 429 && tryCount < maxRetries) {
                    tryCount++
                    response.close()
                    val waitTime = 1500L * tryCount
                    Log.w("Network", "HTTP 429 Too Many Requests. Retrying in ${waitTime}ms...")
                    Thread.sleep(waitTime)
                    continue
                }
                return response
            } catch (e: Exception) {
                if (tryCount < maxRetries && (e is java.net.SocketTimeoutException || e is java.io.IOException)) {
                    tryCount++
                    Log.w("Network", "Network Timeout/IO Error: ${e.message}. Retrying attempt $tryCount/$maxRetries...")
                    Thread.sleep(1000L)
                } else {
                    throw e
                }
            }
        }
    }
}

class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "en-US,en;q=0.9,pt-BR;q=0.8,pt;q=0.7")
            .header("Content-Type", "application/json")
            .header("Origin", "https://anilist.co")
            .header("Referer", "https://anilist.co/")
            .build()
        return chain.proceed(newRequest)
    }
}

object RetrofitClient {
    private const val BASE_URL = "https://graphql.anilist.co/"
    private const val CDN_BASE_URL = "https://cdn.jsdelivr.net/"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("AniListHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val builder = OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor())
            .addInterceptor(RateLimitRetryInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)

        appContext?.let { ctx ->
            try {
                val cacheDir = File(ctx.cacheDir, "okhttp_cache")
                val cache = Cache(cacheDir, 50L * 1024L * 1024L)
                builder.cache(cache)
            } catch (_: Exception) {}
        }

        builder.build()
    }

    val aniListApiService: AniListApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AniListApiService::class.java)
    }

    val bangumiApiService: BangumiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(CDN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BangumiApiService::class.java)
    }

    val jikanApiService: JikanApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.jikan.moe/v4/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JikanApiService::class.java)
    }
}
